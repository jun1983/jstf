package com.jstf.mockproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.jstf.config.JConfig;
import com.jstf.utils.JLogger;

import ch.qos.logback.classic.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.RequestFilterAdapter;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.filters.ResponseFilterAdapter;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

public class JMockProxy {
	private boolean chainedOnly = false;
	private String upstreamProxy = null;
	private boolean isAvailable = false;
	
	private Logger logger = JLogger.getLogger();
	
	private BrowserMobProxy internalProxy;
	private BrowserMobProxy externalProxy;
	
	private static List<JMockProxy> jMockProxies = new ArrayList<>();
	
	public static JMockProxy retrieve() throws Exception {
		for(JMockProxy jMockProxy : jMockProxies){
			if(jMockProxy.isAvailable()){
				jMockProxy.setAvailable(false);
				return jMockProxy;
			}
		}
		
		JMockProxy jMockProxy = new JMockProxy();
		return jMockProxy.start();
	}
	
	private boolean isCapturingHar = false;
	
	public JMockProxy() throws Exception {
		if(!JConfig.IS_MOCK_PROXY_ENABLED) {
			throw new Exception("JMockProxy is not enabled in configuration. Please check 'is_mock_proxy_enabled' setting.");
		}
		
		if(JConfig.IS_PROXY_ENABLED) {
			this.upstreamProxy = JConfig.PROXY_ADDR;
			this.chainedOnly = JConfig.PROXY_BYPASS == null || JConfig.PROXY_BYPASS.isEmpty();
		}
	}
	
	public JMockProxy(String upstreamProxy) {
		this.chainedOnly = true;
		this.upstreamProxy = upstreamProxy;
	}
	
	public JMockProxy(String upstreamProxy, Boolean chainedOnly) {
		this.chainedOnly = chainedOnly;
		this.upstreamProxy = upstreamProxy;
	}
	
	public JMockProxy start() throws Exception{
		if(!chainedOnly) {
			internalProxy = intiateBrowserMobProxy();
			logger.info("Direct Mock Proxy: " + getDirectMockAddress() + " connecting to internet directly");
		}
		
		if(upstreamProxy!=null) {
			externalProxy = intiateBrowserMobProxy(upstreamProxy);
			logger.info("Chained Mock Proxy: " + getChainedMockAddress() + " chain upstream to " + getUpstreamProxy());
		}
		
		if(JConfig.IS_CORS_ENABLED) {
			enableCORS();
		}
		return this;
	}
	
	public void setAvailable(boolean b) {
		this.isAvailable = b;
	}
	
	public boolean isAvailable() {
		return this.isAvailable;
	}
	
	private BrowserMobProxy intiateBrowserMobProxy() throws Exception{
		return intiateBrowserMobProxy(null);
	}
	
	private BrowserMobProxy intiateBrowserMobProxy(String chainProxy) throws Exception{
		BrowserMobProxy proxy = new BrowserMobProxyServer();
		proxy.setTrustAllServers(true);
		if(chainProxy!=null) {
			String chainedProxyAddress = chainProxy.split(":")[0];
			String chainedProxyPort = chainProxy.split(":")[1];
			proxy.setChainedProxy(new InetSocketAddress(chainedProxyAddress, Integer.parseInt(chainedProxyPort)));
		}
		
	    proxy.start(0);
		
		return proxy;
	}
	
	/**
	 * @return return internal proxy instance in ip:port format
	 */
	public String getDirectMockAddress() {
		if(this.internalProxy == null) {
			return null;
		}
		return "127.0.0.1:" + this.internalProxy.getPort();
	}
	
	/**
	 * @return return external proxy instance if enabled. Otherwise return internal proxy instance.
	 */
	public String getChainedMockAddress() {
		if(this.externalProxy == null) {
			return null;
		}
		return "127.0.0.1:" + this.externalProxy.getPort();
	}
	
	/**
	 * get upstream corporate proxy address
	 * @return
	 * @throws Exception
	 */
	public String getUpstreamProxy() throws Exception{
		return upstreamProxy!=null? upstreamProxy : null;
	}
	
	public boolean isCapturingHar() throws Exception{
		return isCapturingHar;
	}
	
	/**
	 * close current proxy instance
	 */
	public void close() throws Exception {
		if(internalProxy!=null) {
			internalProxy.stop();
		}
		
		if(externalProxy!=null) {
			externalProxy.stop();
		}
		jMockProxies.remove(this);
	}
	
	/**
	 * Start to capture HTTP(s) payload to a har file
	 * @param pageName, a key to this capture
	 * @param captureTypes, List of contents intend to capture
	 */
	public void startHarCapture(String pageName, CaptureType...captureTypes) throws Exception {
		this.isCapturingHar = true;
		if(internalProxy!=null) {
			internalProxy.enableHarCaptureTypes(captureTypes);
			internalProxy.newHar(pageName);
		}
		if(externalProxy!=null) {
			internalProxy.enableHarCaptureTypes(captureTypes);
			internalProxy.newHar(pageName);
		}
	}
	
	private String getHar(BrowserMobProxy browserMobProxy) throws Exception {
		Har har = browserMobProxy.getHar();

		java.io.StringWriter writer = new java.io.StringWriter();
		try {
            har.writeTo(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

		return writer.toString();
	}
	
	/**
	 * get all entries captured so far and end current capture.
	 * @return Json array including all requests and responses
	 */
	public JSONArray getAllEntriesFromHar() throws Exception {
		JSONArray combinedEntries = new JSONArray();
		
		if(internalProxy!=null) {
			String internalHarString = getHar(internalProxy);
			org.json.JSONObject internalJson = new org.json.JSONObject(internalHarString);
			JSONArray internalEntries = internalJson.getJSONObject("log").getJSONArray("entries");
			for (int i =0; i<internalEntries.length();i++) {
				combinedEntries.put(internalEntries.getJSONObject(i));
			}
		}
		
		if(externalProxy!=null) {
			String externalHarString = getHar(internalProxy);
			org.json.JSONObject externalJson = new org.json.JSONObject(externalHarString);
			
			JSONArray externalEntries = externalJson.getJSONObject("log").getJSONArray("entries");
			
			for (int i =0; i<externalEntries.length();i++) {
				combinedEntries.put(externalEntries.getJSONObject(i));
			}
		}
		this.isCapturingHar = false;
		return combinedEntries;
	}
	
	/**
	 * get all request entries captured so far and end the current capture.
	 * @return Json array including all requests
	 */
	public JSONArray getAllRequestsFromHar() throws Exception {
		JSONArray entriesArray = getAllEntriesFromHar();
		JSONArray requestArray = new JSONArray();
		for (int i = 0; i < entriesArray.length(); i++) {
			if(entriesArray.getJSONObject(i).has("request")) {
				requestArray.put(entriesArray.getJSONObject(i).get("request"));
			}
		}
		return requestArray;
	}
	
	/**
	 * set http header to all requests
	 * @param header values in Map<String, String> format
	 */
	public void setHttpHeader(Map<String, String> headers) throws Exception {
		if(internalProxy!=null) {
			internalProxy.addHeaders(headers);
		}
		if(externalProxy!=null) {
			externalProxy.addHeaders(headers);
		}
	}
	
	public void addRequestFilter(RequestFilter requestFilter) throws Exception {
		if(internalProxy!=null) {
			internalProxy.addFirstHttpFilterFactory(new RequestFilterAdapter.FilterSource(requestFilter, 16777216));
		}
		if(externalProxy!=null) {
			externalProxy.addFirstHttpFilterFactory(new RequestFilterAdapter.FilterSource(requestFilter, 16777216));
		}
	}
	
	public void addResponseFilter(ResponseFilter responseFilter) throws Exception {
		if(internalProxy!=null) {
			internalProxy.addFirstHttpFilterFactory(new ResponseFilterAdapter.FilterSource(responseFilter, 16777216));
		}
		if(externalProxy!=null) {
			externalProxy.addFirstHttpFilterFactory(new ResponseFilterAdapter.FilterSource(responseFilter, 16777216));
		}
	}
	
	public void rewriteUrl(String matchRegex, String replaceUrl) throws Exception {
		if(internalProxy!=null) {
			internalProxy.rewriteUrl(matchRegex, replaceUrl);
		}
		if(externalProxy!=null) {
			externalProxy.rewriteUrl(matchRegex, replaceUrl);
		}
	}
	
	/**
	 * Remap DNS look up
	 * @param originalDomainName
	 * @param newDomainName or IP
	 */
	public void remapDNSLookUp(String originalDomainName, String destination) {
		if(internalProxy!=null) {
			internalProxy.getHostNameResolver().remapHost(originalDomainName, destination);
		}
		if(externalProxy!=null) {
			externalProxy.getHostNameResolver().remapHost(originalDomainName, destination);
		}
	}
	
	/**
	 * Block URL
	 * @param urlPattern in String
	 */
	public void blacklistRequest(String urlPattern) {
		if(internalProxy!=null) {
			internalProxy.blacklistRequests(urlPattern, 500);
		}
		if(externalProxy!=null) {
			externalProxy.blacklistRequests(urlPattern, 500);
		}
	}
	
	/**
	 * Respond with TEMPORARY_REDIRECT code and given new url. 
	 * @param originalUrlRegex
	 * @param newUrl
	 * @throws Exception
	 */
	public void redirectUrl(String originalUrlRegex, String newUrl) throws Exception {
		addRequestFilter(new RequestFilter() {
			@Override
			public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
				if(messageInfo.getOriginalUrl().matches(originalUrlRegex) && !request.getMethod().equals(HttpMethod.OPTIONS)) {
					logger.info("Receive Matched request:" + messageInfo.getOriginalUrl());
					logger.info("Redirect to:" + newUrl);

					HttpResponse response = createShortCircuitResponse(HttpResponseStatus.TEMPORARY_REDIRECT, null, request, contents, messageInfo);
					response.headers().set("location", newUrl);
				    return response;
				}
				return null;
			}
		});
	}
	
	/**
	 * Silently forward original request to a new url. This interference is not visible in browser
	 * @param originalUrlRegex
	 * @param newUrl
	 * @throws Exception
	 */
	public void forwardUrl(String originalUrlRegex, String newUrl) throws Exception {
		addRequestFilter(new RequestFilter() {
			@Override
			public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
				if(messageInfo.getOriginalUrl().matches(originalUrlRegex) && !request.getMethod().equals(HttpMethod.OPTIONS)) {
					logger.info("Receive Matched request:" + messageInfo.getOriginalUrl());
					logger.info("Forward to:" + newUrl);
					request.setUri(newUrl);
				}
				return null;
			}
		});
	}
	
	public HttpResponse createShortCircuitResponse(HttpResponseStatus responseStatus, String responseContent, HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
		logger.debug("Creating short circuit response to request..");
		logger.debug(request.getMethod().name() + " " + messageInfo.getOriginalUrl());
		
		HttpResponse response;
		if(responseContent==null) {
			response = new DefaultFullHttpResponse(request.getProtocolVersion(), responseStatus);
		}else {
			ByteBuf buf = Unpooled.wrappedBuffer(responseContent.getBytes(StandardCharsets.UTF_8));
			response = new DefaultFullHttpResponse(request.getProtocolVersion(), responseStatus, buf);
		}
		
		addCORSHeader(response, messageInfo);
		logger.debug("Short circuit response return..");

		return response;
	}
	
	public void enableCORS() throws Exception{
		addRequestFilter(new RequestFilter() {
			@Override
			public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
				if(request.getMethod().equals(HttpMethod.OPTIONS)) {
					HttpResponse response = createShortCircuitResponse(HttpResponseStatus.NO_CONTENT, null, request, contents, messageInfo);
					addCORSHeader(response, messageInfo);
					response.headers().set("Access-Control-Max-Age", "3628800");
					return response;
				}
				return null;
			}
		});
	}
	
	private void addCORSHeader(HttpResponse response, HttpMessageInfo messageInfo) {
		String origin = messageInfo.getOriginalRequest().headers().contains("Origin")? messageInfo.getOriginalRequest().headers().get("Origin") : "*";
		response.headers().set("Access-Control-Allow-Origin", origin);		
		response.headers().set("Access-Control-Allow-Credentials", "true");
		String allowHeaders = messageInfo.getOriginalRequest().headers().contains("Access-Control-Request-Headers")?messageInfo.getOriginalRequest().headers().get("Access-Control-Request-Headers") : "*";
		response.headers().set("Access-Control-Allow-Headers", allowHeaders);
		response.headers().set("Access-Control-Allow-Methods", "GET,HEAD,PUT,PATCH,POST,DELETE");
		response.headers().set("Connection", "close");
	}
	
}
