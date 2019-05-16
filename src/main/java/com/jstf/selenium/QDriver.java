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
import com.jstf.config.QConfig;
import com.jstf.mockproxy.QMockProxy;
import com.jstf.utils.QLogger;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.Setter;

public class QDriver {
	@Setter @Getter
	private WebDriver driver;
	@Getter
	private boolean isRemoteDriver = false;
	private BrowserType browserType;
	private String seleniumHub;
	private MutableCapabilities capabilities;
	@Setter @Getter
	private QMockProxy qMockProxy;
	private Logger logger = QLogger.getLogger();
	
	/**
	 * Define a local / remote driver regarding to config
	 * @throws Exception
	*/
	public QDriver() throws Exception {
		this(BrowserType.fromString(QConfig.BROWSER), QConfig.IS_REMOTE_DRIVER);
	}
	
	/**
	 * Define a local driver
	 * @param browserType
	 * @throws Exception 
	 */
	public QDriver(BrowserType browserType) throws Exception {
		this(browserType, false);
	}
	
	private QDriver(BrowserType browserType, Boolean isRemoteDriver) throws Exception{
		this.browserType = BrowserType.fromString(QConfig.BROWSER);
		this.isRemoteDriver = isRemoteDriver;
		
		if(isRemoteDriver) {
			this.seleniumHub = QConfig.SELENIUM_HUB;
			this.capabilities = ConfigHelper.getRemoteCapability(QConfig.REMOTE_DRIVER_CAPABILITY);
		}else {
			this.capabilities = QDriverOptions.getDefaultDriverOptions(browserType);
		}
		
		if(QConfig.IS_MOCK_PROXY_ENABLED) {
			this.qMockProxy = QMockProxy.retrieve();
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
				throw new Exception(QConfig.BROWSER + " is not supported.");
			}
		}
	}
	
	/**
	 * Define a remote driver
	 * @param seleniumHub
	 * @param capabilities
	 * @throws Exception
	 */
	public QDriver(String seleniumHub, DesiredCapabilities capabilities) throws Exception {
		this.seleniumHub = seleniumHub;
		this.capabilities = capabilities;
	}
	
	public QDriver mergeCapabilities(DesiredCapabilities extraCapabilities) throws Exception {
		if(driver!=null) {
			throw new Exception("WebDriver is already started. Capabilities can only be set before starting driver.");
		}
		
		this.capabilities.merge(extraCapabilities);
		return this;
	}
	
	public boolean isMockProxied() {
		return this.qMockProxy != null;
	}
	
	public void closeMockProxy() throws Exception {
		this.qMockProxy.close();
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
		getUrl(url, titleContain, QConfig.ELEMENT_TIMEOUT);
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
		new WebDriverWait(driver, QConfig.ELEMENT_TIMEOUT).until(expectedCondition);
	}
	
	public QElement find(String cssSelector) {
		return find(By.cssSelector(cssSelector));
	}
	
	public QElement find(ElementSelectionType elementSelectionType, String cssSelector) {
		return find(elementSelectionType, By.cssSelector(cssSelector));
	}
	
	public QElement find(By... bys) {
		return find(ElementSelectionType.ALL, bys);
	}
	
	public QElement find(ElementSelectionType elementSelectionType, By... bys) {
		return new QElement(this, elementSelectionType, bys);
	}
	
	public String getTitle() {
		return driver.getTitle();
	}
	
	public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}
	
	public QDriver start() throws Exception{
		if(!isRemoteDriver) {
			QDriverOptions.initDriverBinarySetting(browserType);
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
		
		driver.manage().timeouts().implicitlyWait(QConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(QConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);

		if(QConfig.DEFAULT_WINDOW_SIZE!=null && !QConfig.DEFAULT_WINDOW_SIZE.isEmpty()) {
			int width = Integer.parseInt(QConfig.DEFAULT_WINDOW_SIZE.split("[*]")[0]);
			int height = Integer.parseInt(QConfig.DEFAULT_WINDOW_SIZE.split("[*]")[1]);
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
			if(this.qMockProxy.getChainedMockAddress()==null) {
				//Mockproxy goes directly to internet
				String PROXY = qMockProxy.getDirectMockAddress();
				proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
			 }else if(qMockProxy.getDirectMockAddress()==null) {
				 //Chains all Mockproxy traffic to upstream proxy
				 String PROXY = qMockProxy.getChainedMockAddress();
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
		} else if(QConfig.IS_ZAP_ENABLED) {
			String PROXY = QConfig.ZAP_SERVER;
			proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
		} else if (QConfig.IS_PROXY_ENABLED) {
			String PROXY = QConfig.PROXY_ADDR;
			proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY)
					.setNoProxy(QConfig.PROXY_BYPASS);
		} else {
			return null;
		}
		return proxy;
	}	
}
