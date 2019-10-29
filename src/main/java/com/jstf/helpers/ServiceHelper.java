package com.jstf.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Cookie.Builder;
import org.openqa.selenium.WebDriver;

import com.jstf.config.JConfig;

import lombok.Getter;
import lombok.Setter;

public class ServiceHelper {
	private CloseableHttpClient httpClient;
	private CookieStore cookieStore;
	
	@Getter @Setter
	private int timeout = 20;
	private Map<String, String> customisedHTTPHeaders = new HashMap<>();
	private HttpRequestBase request;
	@Getter @Setter
	private boolean redirectsEnabled = true;
	
	@Getter @Setter
	private boolean closeAfterExecution = true;
	
	public ServiceHelper() {
		cookieStore = new BasicCookieStore();
	    
		final HttpClientBuilder clientBuilder = HttpClients.custom()
												.setRedirectStrategy(new LaxRedirectStrategy())
												.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
												.setDefaultCookieStore(cookieStore);

		if(JConfig.IS_PROXY_ENABLED) {
			String proxyHost = JConfig.PROXY_ADDR.split(":")[0];
			int proxyPort = Integer.parseInt(JConfig.PROXY_ADDR.split(":")[1]);
			HttpHost proxy = new HttpHost(proxyHost, proxyPort); 
			
			if (StringUtils.isNotEmpty(JConfig.PROXY_BYPASS)) {
	            final String[] excludeHosts = JConfig.PROXY_BYPASS.split("[,;]");
	            for(int i=0;i<excludeHosts.length;i++) {
	            		excludeHosts[i] = excludeHosts[i].trim();
	            }

	            HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy) {
	                @Override
	                public HttpRoute determineRoute(final HttpHost host, final org.apache.http.HttpRequest request, final HttpContext context) throws HttpException {
	                    String hostname = (host != null ? host
	                            .getHostName() : null);

	                    if ((excludeHosts != null) && (hostname != null)) {
	                    		for (String excludeHost : excludeHosts) {
	                    			if (hostname.contains(excludeHost.replace("*", ""))) {
	                                // bypass proxy for that hostname
	                                return new HttpRoute(host);
	                            }
	                        }
	                    }
	                    return super.determineRoute(host, request, context);
	                }
	            };

	            clientBuilder.setRoutePlanner(routePlanner);
	        }
		}
		this.httpClient = clientBuilder.build();
	}
	
	public void setHttpRequest(HttpRequestBase request) {
		this.request = request;
	}
	
	public ServiceHelper setHttpHeader(String header, String value) {
		customisedHTTPHeaders.put(header, value);
		return this;
	}
	
	public ServiceHelper setHttpHeaders(Map<String, String> headers) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			customisedHTTPHeaders.put(entry.getKey(), entry.getValue());
		}
		return this;
	}
	
	public Map<String, String> getCustomisedHttpHeaders(){
		return this.customisedHTTPHeaders;
	}
	
	public String get(String urlStr) throws Exception{
		this.request = new HttpGet(urlStr);
		return execute();
	}
	
	public void closeHttpClient() throws IOException {
		if(httpClient!=null) {
			httpClient.close();
		}
	}
	
	public String postJson(String urlStr, JSONObject json) throws Exception{
		HttpPost httpPost = new HttpPost(urlStr);

		// Request parameters and other properties.
		StringEntity se = new StringEntity(json.toString());
		
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(se);
		
        this.request = httpPost;
        return execute();
	}
	
	public String postUrlEncoded(String urlStr, List<NameValuePair> params) throws Exception {
		HttpPost httpPost = new HttpPost(urlStr);
		httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        this.request = httpPost;
		return execute();
	}
	
	public String postPlainText(String urlStr, String text) throws Exception {
		HttpPost httpPost = new HttpPost(urlStr);
		httpPost.setEntity(new StringEntity(text));
        this.request = httpPost;

		return execute();
	}
	
	public String put(String urlStr, List<NameValuePair> params) throws Exception{
		HttpPut httpPut = new HttpPut(urlStr);
		httpPut.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        this.request = httpPut;

        return execute();
	}
	
	public String delete(String urlStr) throws Exception{
		HttpDelete httpDelete = new HttpDelete(urlStr);
        this.request = httpDelete;
		return execute();
	}

	public String execute() throws Exception {
		CloseableHttpResponse response = executeAndReturnResposne();
        try {
	        InputStream in = response.getEntity().getContent();
	        String responseStr = IOUtils.toString(in);
	        IOUtils.closeQuietly(in);
	        return responseStr;
	    }catch(NullPointerException e){
	    		return "";
	    }finally {
	    		response.close();
	    		if(closeAfterExecution) {
	    			this.closeHttpClient();
	    		}
		}
	}
	
	public CloseableHttpResponse executeAndReturnResposne() throws IOException {
		for (Map.Entry<String, String> entry : this.customisedHTTPHeaders.entrySet()) {
			request.setHeader(entry.getKey(), entry.getValue());
		}
		
		org.apache.http.client.config.RequestConfig.Builder configBuilder = RequestConfig.custom()
	            .setConnectTimeout(timeout * 1000)
	            .setConnectionRequestTimeout(timeout * 1000)
	            .setSocketTimeout(timeout * 1000)
	            .setRedirectsEnabled(redirectsEnabled);
		
		RequestConfig config = configBuilder.build();

		request.setConfig(config);
		CloseableHttpResponse response = httpClient.execute(request);
		return response;
	}
	
	public void importCookiesFromDriver(WebDriver driver) {
		Set<Cookie> currentCookies = driver.manage().getCookies();
		
		Iterator<Cookie> itr = currentCookies.iterator();
		String postCookieStr = "";
		while (itr.hasNext()) {
			Cookie cookie = itr.next();
			postCookieStr = postCookieStr + cookie.getName() + "=" + cookie.getValue() + ";";
		}
		if(!postCookieStr.equals("")) {
			this.setHttpHeader("cookie", postCookieStr);
		}
	}
	
	public void appendCookieFromResponseToDriver(CloseableHttpResponse response, WebDriver driver) {
		Header[] setCookies = response.getHeaders("Set-Cookie");
		for (Header setCookie : setCookies) {
			String cookieStr = setCookie.getValue();
			driver.manage().addCookie(parseCookie(cookieStr));
		}
	}
	
	public List<?> getCookies() {
		return cookieStore.getCookies();
	}
	
	private Cookie parseCookie(String cookieStr) {
		List<String> items = new LinkedList<String>(Arrays.asList(cookieStr.split(";")));
		String nameValuePair = items.get(0);
		String name = nameValuePair.split("=")[0];
		String value = nameValuePair.split("=")[1];
		items.remove(0);
		
		Builder cookieBuilder = new Cookie.Builder(name, value);
		for (String item : items) {
			if(item.toLowerCase().startsWith("path")) {
				cookieBuilder.path(item.split("=")[1]);
			}
			if(item.equalsIgnoreCase("HttpOnly")) {
				cookieBuilder.isHttpOnly(true);
			}
			if(item.equalsIgnoreCase("Secure")) {
				cookieBuilder.isSecure(true);
			}
		}
		return cookieBuilder.build();
	}
}
