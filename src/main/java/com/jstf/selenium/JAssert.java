package com.jstf.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.config.JConfig;

public class JAssert {
	private JDriver jDriver;
	private WebDriver driver;
	public JAssert(JDriver jDriver) {
		this.jDriver = jDriver;
		this.driver = jDriver.getWebDriver();
	}
	
	public void urlEquals(String url, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.urlToBe(url));
	}
	
	public void urlEquals(String url) throws Exception {
		urlEquals(url, JConfig.ELEMENT_TIMEOUT);
	}
	
	public void urlContains(String url) throws Exception {
		urlContains(url, JConfig.ELEMENT_TIMEOUT);
	}
	
	public void urlContains(String url, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.urlContains(url));
	}
	
	public void urlMatch(String regex, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.urlMatches(regex));
	}
	
	public void urlMatch(String regex) throws Exception {
		urlMatch(regex, JConfig.ELEMENT_TIMEOUT);
	}
	
	public void titleEquals(String title, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.titleIs(title));
	}
	
	public void titleEquals(String title) throws Exception {
		titleEquals(title, JConfig.ELEMENT_TIMEOUT);
	}
	
	public void titleContainsIgnoreCase(String title, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(JExpectedConditions.titleContainsIgnoreCase(title));
	}
	
	public void titleContainsIgnoreCase(String title) throws Exception {
		new WebDriverWait(driver, JConfig.ELEMENT_TIMEOUT).until(JExpectedConditions.titleContainsIgnoreCase(title));
	}
	
	public void titleContains(String title) throws Exception {
		titleContains(title, JConfig.ELEMENT_TIMEOUT);
	}
	
	public void titleContains(String title, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.titleContains(title));
	}
}
