package com.jstf.selenium;

import java.net.URL;
import java.util.concurrent.TimeUnit;

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
import lombok.Getter;
import lombok.Setter;

public class JDriver {
	@Setter @Getter
	private WebDriver driver;
	@Getter
	private boolean isRemoteDriver = false;
	private BrowserType browserType;
	private String seleniumHub;
	private MutableCapabilities capabilities;
	@Setter @Getter
	private JMockProxy jMockProxy;
	private Logger logger = JLogger.getLogger();
	
	/**
	 * Define a local / remote driver regarding to config
	 * @throws Exception
	*/
	public JDriver() throws Exception {
		this(BrowserType.fromString(JConfig.BROWSER), JConfig.IS_REMOTE_DRIVER);
	}
	
	public JDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	/**
	 * Define a local driver
	 * @param browserType
	 * @throws Exception 
	 */
	public JDriver(BrowserType browserType) throws Exception {
		this(browserType, false);
	}
	
	private JDriver(BrowserType browserType, Boolean isRemoteDriver) throws Exception{
		this.browserType = BrowserType.fromString(JConfig.BROWSER);
		this.isRemoteDriver = isRemoteDriver;
		
		if(isRemoteDriver) {
			this.seleniumHub = JConfig.SELENIUM_HUB;
			this.capabilities = ConfigHelper.getRemoteCapability(JConfig.REMOTE_DRIVER_CAPABILITY);
		}else {
			this.capabilities = JDriverOptions.getDefaultDriverOptions(browserType);
		}
		
		if(JConfig.IS_MOCK_PROXY_ENABLED) {
			this.jMockProxy = JMockProxy.retrieve();
		}
		
		Proxy proxy = getProxySetting();
		if(proxy!=null) {
			switch (browserType) {
			case CHROME:
				if(proxy!=null) {
					capabilities.setCapability(CapabilityType.PROXY, proxy);
				}
				break;
			case FIREFOX:
				if(proxy!=null) {
					 if(proxy.getNoProxy()!=null) {
						 String noProxy = proxy.getNoProxy();
						 if(proxy.getNoProxy()!=null) {
							 proxy.setNoProxy(null);
							 ((FirefoxOptions)capabilities).getProfile().setPreference("network.proxy.no_proxies_on", noProxy);
						 }
					 }
				}
				break;
			default:
				throw new Exception(JConfig.BROWSER + " is not supported.");
			}
		}
	}
	
	/**
	 * Define a remote driver
	 * @param seleniumHub
	 * @param capabilities
	 * @throws Exception
	 */
	public JDriver(String seleniumHub, DesiredCapabilities capabilities) throws Exception {
		this.seleniumHub = seleniumHub;
		this.capabilities = capabilities;
	}
	
	public JDriver mergeCapabilities(DesiredCapabilities extraCapabilities) throws Exception {
		if(driver!=null) {
			throw new Exception("WebDriver is already started. Capabilities can only be set before starting driver.");
		}
		
		this.capabilities.merge(extraCapabilities);
		return this;
	}
	
	public boolean isMockProxied() {
		return this.jMockProxy != null;
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
		if(!isRemoteDriver) {
			JDriverOptions.initDriverBinarySetting(browserType);
			switch (browserType) {
			case CHROME:
				driver = new ChromeDriver(capabilities);
				break;
			case FIREFOX:
				driver = new FirefoxDriver(capabilities);
				break;
			case IE:
				driver = new InternetExplorerDriver(capabilities);
				break;
			case SAFARI:
				driver = new SafariDriver(capabilities);
				break;
			default:
				throw new Exception(browserType + " is not supported.");
			}
		}else{
			driver = new RemoteWebDriver(new URL(seleniumHub), capabilities);
		}
		
		driver.manage().timeouts().implicitlyWait(JConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(JConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);

		if(JConfig.DEFAULT_WINDOW_SIZE!=null && !JConfig.DEFAULT_WINDOW_SIZE.isEmpty()) {
			int width = Integer.parseInt(JConfig.DEFAULT_WINDOW_SIZE.split("[*]")[0]);
			int height = Integer.parseInt(JConfig.DEFAULT_WINDOW_SIZE.split("[*]")[1]);
			driver.manage().window().setSize(new Dimension(width, height));
		}else {
			driver.manage().window().maximize();
		}

		logger.info("New " + (isRemoteDriver? "remote" : "local") + " " + (isMockProxied()? "Mock Proxied ":"") + " browser opened. ");
		return this;
	}
	
	public void close() {
		this.driver.quit();
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
