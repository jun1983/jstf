package com.juntools.selenium;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;

public class UsingImplicitWait {
	private WebDriver driver;
	
	public UsingImplicitWait(WebDriver driver, int seconds) {
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
	}
	
	public void close(){
		driver.manage().timeouts().implicitlyWait(JSConfig.TIMEOUT, TimeUnit.SECONDS);
	}
}
