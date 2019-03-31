package com.juntools.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class JDriver {
	private WebDriver driver;
	
	public JDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	public WebDriver getWebDriver() {
		return this.driver;
	}
	
	public void setWebDriver(WebDriver driver) {
		this.driver = driver;
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
}
