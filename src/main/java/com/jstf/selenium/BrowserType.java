package com.jstf.selenium;

import com.jstf.utils.JLogger;

public enum BrowserType {
	CHROME, FIREFOX, IE, SAFARI;
	
	public static BrowserType fromString(String browserName){
        switch (browserName.toLowerCase()) {
		case "chrome":
			return BrowserType.CHROME;
		case "firefox":
			return BrowserType.FIREFOX;
		case "ie":
			return BrowserType.IE;
		case "safari":
			return BrowserType.SAFARI;
		default:
			JLogger.getLogger().error(browserName + " is not supported.");
			System.exit(-10021);
			return null;
		}
	}
}
