package com.jstf.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.config.QConfig;

public class QAssert {
	private QDriver qDriver;
	private WebDriver driver;
	public QAssert(QDriver qDriver) {
		this.qDriver = qDriver;
		this.driver = qDriver.getDriver();
	}
	
	public void urlEquals(String url, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.urlToBe(url));
	}
	
	public void urlEquals(String url) throws Exception {
		urlEquals(url, QConfig.ELEMENT_TIMEOUT);
	}
	
	public void urlContains(String url) throws Exception {
		urlContains(url, QConfig.ELEMENT_TIMEOUT);
	}
	
	public void urlContains(String url, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.urlContains(url));
	}
	
	public void urlMatch(String regex, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.urlMatches(regex));
	}
	
	public void urlMatch(String regex) throws Exception {
		urlMatch(regex, QConfig.ELEMENT_TIMEOUT);
	}
	
	public void titleEquals(String title, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.titleIs(title));
	}
	
	public void titleEquals(String title) throws Exception {
		titleEquals(title, QConfig.ELEMENT_TIMEOUT);
	}
	
	public void titleContainsIgnoreCase(String title, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(QExpectedConditions.titleContainsIgnoreCase(title));
	}
	
	public void titleContainsIgnoreCase(String title) throws Exception {
		new WebDriverWait(driver, QConfig.ELEMENT_TIMEOUT).until(QExpectedConditions.titleContainsIgnoreCase(title));
	}
	
	public void titleContains(String title) throws Exception {
		titleContains(title, QConfig.ELEMENT_TIMEOUT);
	}
	
	public void titleContains(String title, int timeOutInSeconds) throws Exception {
		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.titleContains(title));
	}
}
