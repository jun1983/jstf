package com.jstf.tests;

import java.sql.Driver;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;

import com.jstf.utils.JLogger;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

public class MockProxyTest extends BaseTest {
	
	@org.junit.Test
	public void redirectUrlTest() throws Exception {
		jDriver.getUrl(homepageUrl, "GitHub");
		
		jDriver.getMockProxy().redirectUrl("https://shop.github.com/", "https://www.google.com/");
		jDriver.find(".footer").find(By.linkText("Shop")).click();

		jAssert.titleContains("Google", 30);
	}
	
	@org.junit.Test
	public void captureRequestTest() throws Exception {
		jDriver.getUrl(homepageUrl, "GitHub");
		
		jDriver.getMockProxy().startHarCapture("Shop Request", CaptureType.REQUEST_CONTENT, CaptureType.REQUEST_COOKIES);
		jDriver.find(".footer").find(By.linkText("Shop")).click();
		Thread.sleep(5000);
		JSONArray requestsCaptured = jDriver.getMockProxy().getAllRequestsFromHar();
		for (int i=0;i<requestsCaptured.length(); i++) {
			JSONObject requestJson = requestsCaptured.getJSONObject(i);
			String expectedRequest = "https://shop.github.com/";
			if(requestJson.getString("url").equals(expectedRequest) && requestJson.getString("method").equalsIgnoreCase("GET")) {
				JLogger.getLogger().info("Received request to " + expectedRequest);
				return;
			}
		}
		throw new Exception("shop request not received. Captured requests:" + requestsCaptured);
	}
	
	@org.junit.Test
	public void addHttpRequestHeaderTest() throws Exception {
		jDriver.getMockProxy().addRequestFilter(new RequestFilter() {
			@Override
			public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
				if(messageInfo.getOriginalUrl().equals(homepageUrl)){
					request.headers().add("testHeader", "value");
				}
				return null;
			}
		});
		
		jDriver.getUrl(homepageUrl, "GitHub");
	}
	
	@org.junit.Test
	public void addHttpResponseHeaderTest() throws Exception {
		jDriver.getMockProxy().addResponseFilter(new ResponseFilter() {
			@Override
			public void filterResponse(HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {
				if(messageInfo.getOriginalUrl().equals(homepageUrl)) {
					response.headers().add("testHeader", "value");
				}
			}
		});
		
		jDriver.getUrl(homepageUrl, "GitHub");
	}
}
