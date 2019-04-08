package com.jstf.selenium;

import java.io.File;
import java.util.Calendar;
import java.util.Random;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;

import com.jstf.config.JConfig;
import com.jstf.utils.JLogger;
import com.jstf.utils.OSType;

public class JDriverOptions {
	static MutableCapabilities getDefaultDriverOptions(BrowserType browserType) throws Exception {
		switch (browserType) {
		case CHROME:
			return getDefaultChromeOptions();
		case FIREFOX:
			return getDefaultFirefoxOptions();
		default:
			throw new Exception(JConfig.BROWSER + " is not supported.");
		}
	}
	
	static ChromeOptions getDefaultChromeOptions() {
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
	
	static FirefoxOptions getDefaultFirefoxOptions() {
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
	
	static void initDriverBinarySetting(BrowserType browserType) throws Exception {
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
	
	private static void validateDriverBinarySetting(String envVar, String defaultPath) {
		String driverPath = System.getProperty(envVar);
		if(driverPath==null || driverPath.isEmpty()) {
			driverPath = defaultPath;
			System.setProperty(envVar, defaultPath);
			JLogger.getLogger().warn("Webdriver warning: " + envVar + " is not defined. Using default path:" + defaultPath);
		}
		
		File driverFile=new File(driverPath);
		
		if(!driverFile.exists()) {
    			JLogger.getLogger().error("Critical Error: Selenium web driver binary file is not found at " + driverFile.getAbsolutePath());
	    		System.exit(-10010);
	    }
	}
}
