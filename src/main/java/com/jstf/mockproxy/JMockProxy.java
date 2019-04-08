package com.jstf.mockproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.jstf.config.JConfig;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.RequestFilterAdapter;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.filters.ResponseFilterAdapter;
import net.lightbody.bmp.proxy.CaptureType;

public class JMockProxy {
	private boolean chainedOnly = false;
	private String upstreamProxy = null;
	private boolean isAvailable = false;
	
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
		}
		
		if(upstreamProxy!=null) {
			externalProxy = intiateBrowserMobProxy(upstreamProxy);
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
	 * get all entries captured so far
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
	 * get all request entries captured so far
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
	
}
