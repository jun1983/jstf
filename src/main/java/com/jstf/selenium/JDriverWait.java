package com.jstf.selenium;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;

import com.jstf.config.JConfig;

public class JDriverWait {
	private WebDriver driver;
	
	public JDriverWait(WebDriver driver, int seconds) {
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
	}
	
	public JDriverWait(JDriver qDriver, int seconds) {
		this(qDriver.getDriver(), seconds);
	}
	
	public void close(){
		driver.manage().timeouts().implicitlyWait(JConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);
	}
}
