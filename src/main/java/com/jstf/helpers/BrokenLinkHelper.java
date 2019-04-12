package com.jstf.helpers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.utils.JLogger;

import lombok.Getter;
import lombok.Setter;

public class BrokenLinkHelper {
	private WebDriver driver;
	private WebElement componentElement;
	private HashSet<String> linkSet = new HashSet<String>();
	private List<String> brokenLinkList = new ArrayList<>();
	private BlockingQueue<String> queueOfLinks;
	
	@Getter @Setter
	private int queueSize = 10000;
	@Getter @Setter
	private int retries = 1;
	
	@Getter @Setter
	private int threadAmount = 10;
	
	@Getter @Setter
	private boolean ignoreInaccessibleLink = true;
	
	@Getter @Setter
	private boolean ignoreServerError = true;
	
	public BrokenLinkHelper(WebDriver driver) throws MalformedURLException {
		this(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body"))));
	}
	
	public BrokenLinkHelper(WebElement element) throws MalformedURLException {
		this.driver = ((RemoteWebElement)element).getWrappedDriver();
		this.componentElement = element;
		//Find a links
		List<WebElement> linkElements = componentElement.findElements(By.tagName("a"));
		
		for (WebElement linkElement : linkElements) {
			String linkAddress = linkElement.getAttribute("href");
			if(linkAddress!=null) addLink(linkAddress);
		}
		
		List<WebElement> imageElements = componentElement.findElements(By.tagName("img"));
		for (WebElement imageElement : imageElements) {
			String linkAddress = imageElement.getAttribute("src");
			if(linkAddress!=null) addLink(linkAddress);
		}
		queueOfLinks = new ArrayBlockingQueue<>(queueSize, true, linkSet);
	}
	
	public HashSet<String> getAllLinks() throws MalformedURLException{
		return this.linkSet;
	}
	
	public List<String> scan() throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(threadAmount);
		for (int i = 0; i < threadAmount; i++) {
		    pool.submit(() -> {
		        while (true) {
		            String url = null;
		            try {
		            		url = queueOfLinks.take();
		            		int statusCode = scanUrl(url);
		            		if(statusCode>399) {
		            			brokenLinkList.add(url);
		            		}
		            } catch (InterruptedException e) {
		                break;
		            }
		        }
		    });
		}
		
		while(true) {
			if(queueOfLinks.isEmpty()) {
				pool.shutdown();
				break;
			}
		}
		
		JLogger.getLogger().info(linkSet.size() + " links scanned. " + brokenLinkList.size() + " broken links found.");
		return brokenLinkList;
	}
	
	public void doAssert() throws Exception {
		scan();
		if(brokenLinkList.size()>0) {
			throw new Exception(brokenLinkList.size() + " broken links found as below.\n " + StringUtils.join(brokenLinkList, '\n'));
		}
		return;
	}
	
	private int scanUrl(String url){
		return scanUrl(url, 0);
	}
	
	private int scanUrl(String url, int retry){
		if(retry > retries ) {
			return -1;
		}
		
		ServiceHelper serviceHTTPHelper = new ServiceHelper();
		HttpHead httpHead = new HttpHead(url);
		serviceHTTPHelper.setHttpRequest(httpHead);
		CloseableHttpResponse response=null;
		try {
			response = serviceHTTPHelper.executeAndReturnResposne();
			return response.getStatusLine().getStatusCode();
		}catch(Exception e){
			return scanUrl(url, retry + 1);
		}finally {
			try {
				if(response!=null) {
					response.close();
				}
				serviceHTTPHelper.closeHttpClient();
			} catch(Exception e) {}
		}
	}

	private void addLink(String linkAddress) throws MalformedURLException {
		if(linkAddress==null) {
			System.out.println("null");
			return;
		}
		URL currentUrl = new URL(driver.getCurrentUrl());
		String protocol = currentUrl.getProtocol();
		String host = currentUrl.getHost();
		
		if(linkAddress.startsWith("http")) {
			linkSet.add(linkAddress);
		}else if(linkAddress.startsWith("/")) {
			linkSet.add(protocol + "://" + host + linkAddress);
		}else if(linkAddress.startsWith("//")) {
			linkSet.add(protocol + ":" + linkAddress);
		}else {
			return;
		}
	}
}


