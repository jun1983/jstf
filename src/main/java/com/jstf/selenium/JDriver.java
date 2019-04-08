package com.jstf.selenium;

import java.io.File;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;

import com.jstf.config.JConfig;
import com.jstf.mockproxy.JMockProxy;
import com.jstf.utils.JLogger;
import com.jstf.utils.OSType;

import ch.qos.logback.classic.Logger;

public class JDriver {
	private WebDriver driver;
	private JMockProxy jMockProxy;
	private Logger logger = JLogger.getLogger();
	
	public JDriver() throws Exception {
		if(JConfig.IS_MOCK_PROXY_ENABLED) {
			this.jMockProxy = JMockProxy.retrieve();
		}
	}
	
	public JDriver(JMockProxy jMockProxy) {
		this.jMockProxy = jMockProxy;
	}
	
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
	
	public JDriver start() throws Exception{
		BrowserType browserType = BrowserType.fromString(JConfig.BROWSER);
		return start(browserType);
	}

	public JDriver start(MutableCapabilities driverOptions) throws Exception{
		BrowserType browserType = BrowserType.fromString(JConfig.BROWSER);
		return start(browserType, driverOptions);
	}
	
	public JDriver start(BrowserType browserType) throws Exception{
		return start(browserType, getDefaultDriverOptions(browserType));
	}
	
	public JDriver start(BrowserType browserType, MutableCapabilities driverOptions) throws Exception {
		WebDriver driver;
		
		initDriverBinarySetting(browserType);
		Proxy proxy = getProxySetting();
		
		switch (browserType) {
		case CHROME:
			ChromeOptions chromeOptions = (ChromeOptions) driverOptions;
			if(proxy!=null) {
				 chromeOptions.setCapability(CapabilityType.PROXY, proxy);
			 }
			 driver = new ChromeDriver(chromeOptions);
			 break;
		case FIREFOX:
			FirefoxOptions firefoxOptions = (FirefoxOptions) driverOptions;
			 if(proxy!=null) {
				 firefoxOptions.setCapability(CapabilityType.PROXY, proxy);
				 if(proxy.getNoProxy()!=null) {
					 String noProxy = proxy.getNoProxy();
					 if(proxy.getNoProxy()!=null) {
						 proxy.setNoProxy(null);
						 firefoxOptions.getProfile().setPreference("network.proxy.no_proxies_on", noProxy);
					 }
				 }
			 }
			 driver = new FirefoxDriver(firefoxOptions);
			break;
		default:
			throw new Exception(browserType + " is not supported.");
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

		logger.info("New " + (isMockProxied()? "Mock Proxied ":"") + "browser opened. ");
		this.driver = driver;
		return this;
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
	
	private void initDriverBinarySetting(BrowserType browserType) throws Exception {
		switch (browserType) {
		case CHROME:
			validateDriverBinarySetting("webdriver.chrome.driver", "drivers/chromedriver" + (JConfig.OS.equals(OSType.WINDOWS)? ".exe" : ""));
			break;
		case FIREFOX:
			validateDriverBinarySetting("webdriver.gecko.driver", "drivers/geckodriver" + (JConfig.OS.equals(OSType.WINDOWS)? ".exe" : ""));
			break;
		default:
			throw new Exception(JConfig.BROWSER + " is not supported.");
		}
	}
	
	private void validateDriverBinarySetting(String envVar, String defaultPath) {
		String driverPath = System.getProperty(envVar);
		if(driverPath==null || driverPath.isEmpty()) {
			driverPath = defaultPath;
			System.setProperty(envVar, defaultPath);
			JLogger.getLogger().warn("Webdriver warning: " + envVar + " is not defined. Using default path:" + defaultPath);
		}
		
		File driverFile=new File(driverPath);
		
		if(!driverFile.exists()) {
    			logger.error("Critical Error: Selenium web driver binary file is not found at " + driverFile.getAbsolutePath());
	    		System.exit(-10010);
	    }
	}
	
	private MutableCapabilities getDefaultDriverOptions(BrowserType browserType) throws Exception {
		switch (browserType) {
		case CHROME:
			return getDefaultChromeOptions();
		case FIREFOX:
			return getDefaultFirefoxOptions();
		default:
			throw new Exception(JConfig.BROWSER + " is not supported.");
		}
	}
	
	private ChromeOptions getDefaultChromeOptions() {
		ChromeOptions chromeOptions = new ChromeOptions();
		 chromeOptions.addArguments("test-type");
		 chromeOptions.addArguments("–-disable-web-security");
		 
		 String tmpProfilePath = System.getProperty("user.dir") + "/target/tmpChromeProfile/";
		 String timeStamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
		 String chrome_user_dir = tmpProfilePath + timeStamp + "-" + String.valueOf(100000 + new Random().nextInt(900000));
		 chromeOptions.addArguments("--user-data-dir=" + chrome_user_dir);
		 System.setProperty("webdriver.chrome.args", "--disable-logging");
		 System.setProperty("webdriver.chrome.silentOutput", "true");
		 
		 chromeOptions.addArguments("--profile-directory=Default");
		 
		 //System.out.println(chrome_user_dir);
		 chromeOptions.addArguments("--ignore-certificate-errors");
		 chromeOptions.addArguments("–-allow-running-insecure-content");
		 chromeOptions.addArguments("--dns-prefetch-disable");
		 chromeOptions.addArguments("--silent");
        chromeOptions.addArguments("--disable-notifications");
		 chromeOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		 chromeOptions.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, "none");
		 
		 //Headless mode
		 if(JConfig.IS_HEADLESS_BROWSER) {
			 chromeOptions.addArguments("--headless");
		 }
		 return chromeOptions;
	}
	
	private FirefoxOptions getDefaultFirefoxOptions() {
		FirefoxOptions firefoxOptions = new FirefoxOptions();
		if(JConfig.IS_HEADLESS_BROWSER) {
			firefoxOptions.addArguments("--headless");
		}
		FirefoxProfile profile = new FirefoxProfile();
		
		profile.setPreference("geo.prompt.testing", true);
		profile.setPreference("geo.prompt.testing.allow", true);
		profile.setPreference("devtools.jsonview.enabled", false);
		profile.setPreference("browser.privatebrowsing.autostart", true);
		//firefoxOptions.addPreference("log", "{level: trace}");
		firefoxOptions.setCapability(FirefoxDriver.PROFILE, profile);
		firefoxOptions.setCapability("marionette", true);
		firefoxOptions.setCapability("acceptSslCerts", true);
		return firefoxOptions;
	}
}
