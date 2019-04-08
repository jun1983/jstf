package com.jstf.selenium;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;

import com.jstf.config.JConfig;

public class JElementWait {
	private WebDriver driver;
	
	public JElementWait(WebDriver driver, int seconds) {
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
	}
	
	public void close(){
		driver.manage().timeouts().implicitlyWait(JConfig.ELEMENT_TIMEOUT, TimeUnit.SECONDS);
	}
}
