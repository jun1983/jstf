package com.jstf.selenium;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.jws.WebResult;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.config.ConfigHelper;
import com.jstf.config.JConfig;
import com.jstf.mockproxy.JMockProxy;
import com.jstf.utils.JLogger;

import ch.qos.logback.classic.Logger;

public class JDriver {
	private WebDriver driver;
	private boolean isRemoteDriver = false;
	private JMockProxy jMockProxy;
	private Logger logger = JLogger.getLogger();
	
	public JDriver() throws Exception {
		if(JConfig.IS_MOCK_PROXY_ENABLED) {
			this.jMockProxy = JMockProxy.retrieve();
		}
		
		if(JConfig.IS_REMOTE_DRIVER) {
			this.isRemoteDriver = true;
		}
	}
	
	/**
	 * Define a remote jDriver. This will ignore 'browser' setting in configuration. 
	 * @param remoteHub
	 * @param desiredCapabilities
	 * @throws Exception
	 */
	
	/**
	 * 
	 * @param jMockProxy
	 */
	public JDriver(JMockProxy jMockProxy) {
		this.jMockProxy = jMockProxy;
	}
	
	/**
	 * 
	 * @return Selenium Web Driver
	 */
	public WebDriver getWebDriver() {
		return this.driver;
	}
	
	public void setWebDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	public boolean isMockProxied() {
		return this.jMockProxy!=null;
	}
	
	public JMockProxy getMockProxy() {
		return this.jMockProxy;
	}
	
	public void closeMockProxy() throws Exception {
		this.jMockProxy.close();
	}
	
	public void getUrl(String url) throws Exception {
		this.driver.get(url);
	}
	
	/**
	 * 
	 * @param url destination url
	 * @param titleContain string should be contained in page title
	 * @throws Exception
	 */
	public void getUrl(String url, String titleContain) throws Exception {
		getUrl(url, titleContain, JConfig.ELEMENT_TIMEOUT);
	}

	/**
	 * 
	 * @param url destination url
	 * @param titleContain string should be contained in page title 
	 * @param timeOutInSeconds wait time
	 * @throws Exception
	 */
	public void getUrl(String url, String titleContain, int timeOutInSeconds) throws Exception {
		ExpectedCondition<Boolean> expectedCondition = ExpectedConditions.titleContains(titleContain);
		getUrl(url, expectedCondition, timeOutInSeconds);
	}
	
	/**
	 * 
	 * @param url destination url
	 * @param expectedCondition wait for condition after driver.get()
	 * @param timeOutInSeconds time out in seconds
	 */
	public void getUrl(String url, ExpectedCondition<Boolean> expectedCondition, int timeOutInSeconds) {
		this.driver.get(url);
		new WebDriverWait(driver, timeOutInSeconds).until(expectedCondition);
	}
	
	public void getUrl(String url, ExpectedCondition<Boolean> expectedCondition) {
		this.driver.get(url);
		new WebDriverWait(driver, JConfig.ELEMENT_TIMEOUT).until(expectedCondition);
	}
	
	public JElement find(String cssSelector) {
		return find(By.cssSelector(cssSelector));
	}
	
	public JElement find(ElementSelectionType elementSelectionType, String cssSelector) {
		return find(elementSelectionType, By.cssSelector(cssSelector));
	}
	
	public JElement find(By... bys) {
		return find(ElementSelectionType.ALL, bys);
	}
	
	public JElement find(ElementSelectionType elementSelectionType, By... bys) {
		return new JElement(this, elementSelectionType, bys);
	}
	
	public String getTitle() {
		return driver.getTitle();
	}
	
	public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}
	
	public JDriver start() throws Exception{
		BrowserType browserType = BrowserType.fromString(JConfig.BROWSER);
		if(!JConfig.IS_REMOTE_DRIVER) {
			return startLocal(browserType);
		}else{
			return startRemote();
		}
	}
	
	public JDriver startLocal(BrowserType browserType) throws Exception{
		return startLocal(browserType, null);
	}
	
	public JDriver startLocal(BrowserType browserType, MutableCapabilities extraCapabilities) throws Exception {
		this.driver = startLocalDriver(browserType, extraCapabilities);
		initWebDriver();
		return this;
	}
	
	public JDriver startRemote() throws Exception{
		DesiredCapabilities remoteCapabilities = ConfigHelper.getRemoteCapability(JConfig.REMOTE_DRIVER_CAPABILITY);
		Proxy proxy = getProxySetting();
		remoteCapabilities.setCapability(CapabilityType.PROXY, proxy);
		return startRemote(BrowserType.fromString(JConfig.BROWSER), remoteCapabilities);
	}
	
	public JDriver startRemote(BrowserType browserType, MutableCapabilities remoteCapabilities) throws Exception{
		System.out.println(remoteCapabilities);
		Proxy proxy = getProxySetting();
		if(proxy!=null) {
			switch (browserType) {
			case CHROME:
				if(proxy!=null) {
					remoteCapabilities.setCapability(CapabilityType.PROXY, proxy);
				}
				break;
			case FIREFOX:
				if(proxy!=null) {
					 if(proxy.getNoProxy()!=null) {
						 String noProxy = proxy.getNoProxy();
						 if(proxy.getNoProxy()!=null) {
							 proxy.setNoProxy(null);
							 ((FirefoxOptions)remoteCapabilities).getProfile().setPreference("network.proxy.no_proxies_on", noProxy);
						 }
					 }
				}
				break;
			default:
				throw new Exception(JConfig.BROWSER + " is not supported.");
			}
		}
		
		this.driver = new RemoteWebDriver(new URL(JConfig.SELENIUM_HUB), remoteCapabilities);
		initWebDriver();
		return this;
	}
	
	private void initWebDriver() {
		driver.manage().timeouts().implicitlyWait(JConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(JConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);

		if(JConfig.DEFAULT_WINDOW_SIZE!=null && !JConfig.DEFAULT_WINDOW_SIZE.isEmpty()) {
			int width = Integer.parseInt(JConfig.DEFAULT_WINDOW_SIZE.split("[*]")[0]);
			int height = Integer.parseInt(JConfig.DEFAULT_WINDOW_SIZE.split("[*]")[1]);
			driver.manage().window().setSize(new Dimension(width, height));
		}else {
			driver.manage().window().maximize();
		}

		logger.info("New " +  (isMockProxied()? "Mock Proxied ":"") + "browser opened. ");
	}
	
	private WebDriver startLocalDriver(BrowserType browserType, MutableCapabilities extraCapabilities) throws Exception {
		WebDriver driver = null;
		MutableCapabilities driverOptions;
		Proxy proxy = getProxySetting();
		switch (browserType) {
		case CHROME:
			driverOptions = JDriverOptions.getDefaultChromeOptions();
			if(proxy!=null) {
				driverOptions.setCapability(CapabilityType.PROXY, proxy);
			}
			break;
		case FIREFOX:
			driverOptions = JDriverOptions.getDefaultFirefoxOptions();
			if(proxy!=null) {
				driverOptions.setCapability(CapabilityType.PROXY, proxy);
				 if(proxy.getNoProxy()!=null) {
					 String noProxy = proxy.getNoProxy();
					 if(proxy.getNoProxy()!=null) {
						 proxy.setNoProxy(null);
						 ((FirefoxOptions)driverOptions).getProfile().setPreference("network.proxy.no_proxies_on", noProxy);
					 }
				 }
			}
			break;
		default:
			throw new Exception(JConfig.BROWSER + " is not supported.");
		}
		
		if(extraCapabilities!=null) {
			driverOptions.merge(extraCapabilities);
		}
		
		JDriverOptions.initDriverBinarySetting(browserType);
		switch (browserType) {
		case CHROME:
			driver = new ChromeDriver(driverOptions);
			break;
		case FIREFOX:
			driver = new FirefoxDriver(driverOptions);
			break;
		case IE:
			driver = new InternetExplorerDriver(driverOptions);
			break;
		case SAFARI:
			driver = new SafariDriver(driverOptions);
			break;
		default:
			throw new Exception(browserType + " is not supported.");
		}
		return driver;
	}
	
	public void close() {
		this.getWebDriver().quit();
	}
	
	private Proxy getProxySetting() throws Exception {
		Proxy proxy = new Proxy();
		if(isMockProxied()) {
			if(this.jMockProxy.getChainedMockAddress()==null) {
				//Mockproxy goes directly to internet
				String PROXY = jMockProxy.getDirectMockAddress();
				proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
			 }else if(jMockProxy.getDirectMockAddress()==null) {
				 //Chains all Mockproxy traffic to upstream proxy
				 String PROXY = jMockProxy.getChainedMockAddress();
				 proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
			 }
			 else {
				 throw new Exception("Bypass upstream proxy is not supported. Coming soon...");
				 /*
				 //Use 2 mockproxy instances and pac proxy file to dynamically traffic
				 String internalProxy = jMockProxy.getDirectMockAddress();
				 String externalProxy = jMockProxy.getChainedMockAddress();
				
				 //Generate proxy pac file
				 //Read template pac file
				 ClassLoader classLoader = getClass().getClassLoader();
				 String content = IOUtils.toString(classLoader.getResource("proxyTemplate.pac").openStream(), "UTF-8");
				 
				 //Replace placeholders
				 content = content.replaceAll("\\$internalProxy", internalProxy);
				 content = content.replaceAll("\\$externalProxy", externalProxy);
				 String pacFileName = String.valueOf(Thread.currentThread().getId()) + ".pac";
				 String pacFilePath = "target/" + pacFileName;
				 IOUtils.write(content, new FileOutputStream(pacFilePath), "UTF-8");
				 String pacFile = "file://" + new File(pacFilePath).getAbsolutePath();
				 proxy.setProxyAutoconfigUrl(pacFile);
				 */
			 }
		} else if(JConfig.IS_ZAP_ENABLED) {
			String PROXY = JConfig.ZAP_SERVER;
			proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
		} else if (JConfig.IS_PROXY_ENABLED) {
			String PROXY = JConfig.PROXY_ADDR;
			proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY)
					.setNoProxy(JConfig.PROXY_BYPASS);
		} else {
			return null;
		}
		return proxy;
	}	
}
