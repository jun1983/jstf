package com.jstf.selenium;

import com.jstf.utils.JLogger;

public enum BrowserType {
	CHROME, FIREFOX, REMOTE;
	
	public static BrowserType fromString(String browserName){
        switch (browserName.toLowerCase()) {
		case "chrome":
			return BrowserType.CHROME;
		case "firefox":
			return BrowserType.FIREFOX;
		case "remote":
			return BrowserType.REMOTE;
		default:
			JLogger.getLogger().error(browserName + " is not supported.");
			System.exit(-10021);
			return null;
		}
	}
}
