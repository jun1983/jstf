package com.jstf.tests;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;

import com.jstf.selenium.JAssert;
import com.jstf.selenium.JDriver;
import com.jstf.utils.JLogger;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

public class MockProxyTest extends BaseTest {
	@Before
	public void setUp() throws Exception {
		jDriver = new JDriver();
		jDriver.start();
		jAssert = new JAssert(jDriver);
		
	}

	@After
	public void tearDown() throws Exception {
		jDriver.closeMockProxy();
		jDriver.close();
	}
	
	@org.junit.Test
	public void redirectUrlTest() throws Exception {
		jDriver.getUrl(homepageUrl, "GitHub");
		
		jDriver.getJMockProxy().redirectUrl("https://shop.github.com/", "https://www.google.com/");
		jDriver.find(".footer").find(By.linkText("Shop")).click();

		jAssert.titleContains("Google", 30);
	}
	
	@org.junit.Test
	public void captureRequestTest() throws Exception {
		jDriver.getUrl(homepageUrl, "GitHub");
		
		jDriver.getJMockProxy().startHarCapture("Shop Request", CaptureType.REQUEST_CONTENT, CaptureType.REQUEST_COOKIES);
		jDriver.find(".footer").find(By.linkText("Shop")).click();
		Thread.sleep(5000);
		JSONArray requestsCaptured = jDriver.getJMockProxy().getAllRequestsFromHar();
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
		jDriver.getJMockProxy().addRequestFilter(new RequestFilter() {
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
		jDriver.getJMockProxy().addResponseFilter(new ResponseFilter() {
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
