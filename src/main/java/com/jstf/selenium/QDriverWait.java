package com.jstf.selenium;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;

import com.jstf.config.QConfig;

public class QDriverWait {
	private WebDriver driver;
	
	public QDriverWait(WebDriver driver, int seconds) {
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
	}
	
	public QDriverWait(QDriver qDriver, int seconds) {
		this(qDriver.getDriver(), seconds);
	}
	
	public void close(){
		driver.manage().timeouts().implicitlyWait(QConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);
	}
}
