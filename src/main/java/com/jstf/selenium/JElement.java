package com.jstf.selenium;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.config.JConfig;

import lombok.Cleanup;

public class JElement {
	private static final int retryInterval = 100;
	
	private List<JSelector> byList = new ArrayList<>();

	private JDriver jd;
	private WebDriver driver;
	
	private Exception resultException = null;

	
	public JElement(JDriver jd, By... bys) {
		this(jd, ElementSelectionType.ALL, bys);
	}
	
	public JElement(JElement jElement, String cssSelector) {
		this(jElement, By.cssSelector(cssSelector));
	}
	
	public JElement(JElement jElement, ElementSelectionType elementSelectionType, String cssSelector) {
		this(jElement, elementSelectionType, By.cssSelector(cssSelector));
	}
	
	public JElement(JElement jElement, By... bys) {
		this(jElement, ElementSelectionType.ALL, bys);
	}
	
	public JElement(JDriver jd, ElementSelectionType elementSelectionType, By... bys) {
		this.jd = jd;
		this.driver = jd.getWebDriver();
		
		for(int i=0;i<bys.length;i++) {
			if(i==bys.length-1) {
				byList.add(new JSelector(bys[i], elementSelectionType));
			}else {
				byList.add(new JSelector(bys[i], ElementSelectionType.ALL));
			}
		}
	}
	
	public JElement(JElement jElement, ElementSelectionType elementSelectionType, By... bys) {
		this.byList = new ArrayList<>(jElement.byList);
		this.jd = jElement.getJDriver();
		this.driver = jd.getWebDriver();
		
		for(int i=0;i<bys.length;i++) {
			if(i==bys.length-1) {
				byList.add(new JSelector(bys[i], elementSelectionType));
			}else {
				byList.add(new JSelector(bys[i], ElementSelectionType.ALL));
			}
		}
	}
	
	public JDriver getJDriver() {
		return this.jd;
	}
	
	public JElement find(By... bys) {
		return find(ElementSelectionType.ALL, bys);
	}
	
	public JElement find(ElementSelectionType elementSelectionType, By... bys) {
		return new JElement(this, elementSelectionType, bys);
	}
		
	public JElement find(String cssSelector) {
		return find(ElementSelectionType.ALL, cssSelector);
	}
	
	public JElement find(ElementSelectionType elementSelectionType, String cssSelector) {
		return new JElement(this, cssSelector);
	}
	
	public List<WebElement> getWebElements() throws Exception {
		@Cleanup JElementWait jElementWait = new JElementWait(driver, 1);
		List<WebElement> elements = null;

		for (JSelector qBy : byList) {
			if(elements==null) {
				elements = driver.findElements(qBy.getBy());
				elements = filterElementSelection(elements, qBy.getElementSelectionType());
			}else {
				if(elements.size()==0) {
					return elements;
				}
				elements = getSubElements(elements, qBy);
			}
		}
		return elements;
	}
	
	public List<WebElement> filterElementSelection(List<WebElement> elements, ElementSelectionType elementSelectionType){
		List<WebElement> filteredElements = new ArrayList<>();
		switch(elementSelectionType) {
			case FIRST:
				filteredElements.add(elements.get(0));
				break;
			case LAST:
				filteredElements.add(elements.get(elements.size()-1));
				break;
			case RANDOM:
				int randomIndex = (int) (Math.random() * elements.size());
				filteredElements.add(elements.get(randomIndex));
				break;
			default:
				filteredElements = new ArrayList<>(elements);
		}
		return filteredElements;
	}
	
	public WebElement getWebElement() throws Exception {
		List<WebElement> webElements = getWebElements();
		int size = webElements.size();
		if(size==0) {
			throw new Exception("This element is not found. By List:" + this.byList);
		}
		if(size>1) {
			throw new Exception(size + " elements are found. By List:" + this.byList);
		}
		return webElements.get(0);
	}
	
	public WebElement getRandomElement() throws Exception {
		List<WebElement> elements = getWebElements();
		if(elements.size()==0) {
			throw new Exception("No element found.");
		}
		int randomIndex = (int) (Math.random() * elements.size());

		return elements.get(randomIndex);
	}
	
	private List<WebElement> getSubElements(List<WebElement> elements, JSelector qBy){
		@Cleanup JElementWait usingImplicitWait = new JElementWait(jd.getWebDriver(), 1);
		List<WebElement> results = new ArrayList<>();
		for (WebElement element : elements) {
			results.addAll(element.findElements(qBy.getBy()));
		}
		results = filterElementSelection(results, qBy.getElementSelectionType());
		return results;
	}
	
	public JElement parent() throws Exception{
		return find(By.xpath("./.."));
	}

	public JElement clear() throws Exception{
		isDisplayed();
		Exception resultException = null;
		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				focus();
				getWebElement().clear();
				return this;
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	public JElement click() throws Exception {
		assertDisplayed();
		Exception resultException = null;
		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				focus();
				getWebElement().click();
				return this;
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	public JElement hover() throws Exception {
		return hover(null);
	}
	
	public JElement hover(ExpectedCondition<Boolean> expectedCondition) throws Exception {		
		focus();
		Exception resultException = null;
		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				new Actions(driver).moveToElement(this.getWebElement()).perform();
				if(expectedCondition!=null) {
					new WebDriverWait(driver, 1).until(expectedCondition);
				}
				return this;
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	public String getAttribute(String name) throws Exception{
		assertExists();
		Exception resultException = null;
		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				return getWebElement().getAttribute(name);
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	public String getTagName() throws Exception {
		assertExists();
		Exception resultException = null;
		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				return getWebElement().getTagName();
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	public String getText() throws Exception{
		assertExists();
		Exception resultException = null;
		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				return getWebElement().getText();
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	public String getValue() throws Exception{
		assertExists();
		Exception resultException = null;
		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				return getWebElement().getAttribute("value");
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	public boolean isDisplayed() throws Exception {
		return isDisplayed(JConfig.ELEMENT_TIMEOUT);
	}
	
	public boolean isDisplayed(int timeoutInSeconds) throws Exception{
		try {
			return new WebDriverWait(driver, timeoutInSeconds).until(JExpectedConditions.isDisplayed(this));
		}catch (TimeoutException e) {
			return false;
		}
	}
	
	public boolean exists() throws Exception {
		return exists(JConfig.ELEMENT_TIMEOUT);
	}
	
	public boolean exists(int timeoutInSeconds) throws Exception{
		try {
			return new WebDriverWait(driver, timeoutInSeconds).until(JExpectedConditions.exists(this));
		}catch (TimeoutException e) {
			return false;
		}
	}
	
	public boolean isVisibleInViewport()throws Exception{
		return (Boolean)((JavascriptExecutor)driver).executeScript(
			      "var elem = arguments[0],                 " +
			      "  box = elem.getBoundingClientRect(),    " +
			      "  cx = box.left + box.width / 2,         " +
			      "  cy = box.top + box.height / 2,         " +
			      "  e = document.elementFromPoint(cx, cy); " +
			      "for (; e; e = e.parentElement) {         " +
			      "  if (e === elem)                        " +
			      "    return true;                         " +
			      "}                                        " +
			      "return false;                            "
			      , getWebElement());
	}
	
	public boolean isEnabled() throws Exception {
		return isEnabled(JConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean isEnabled(int timeoutInSeconds) throws Exception{
		try {
			return new WebDriverWait(driver, timeoutInSeconds).until(JExpectedConditions.isEnabled(this));
		}catch (TimeoutException e) {
			return false;
		}
	}
	
	public boolean textEquals(String text) throws Exception {
		return textEquals(text, JConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean textEquals(String text, int timeoutInSeconds) throws Exception {
		return new WebDriverWait(driver, timeoutInSeconds).until(JExpectedConditions.textEquals(this, text));
	}
	
	public boolean textNotEquals(String text) throws Exception {
		return textNotEquals(text, JConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean textNotEquals(String text, int timeoutInSeconds) throws Exception {
		return new WebDriverWait(driver, timeoutInSeconds).until(JExpectedConditions.textNotEquals(this, text));
	}
	
	public boolean valueEquals(String value) throws Exception {
		return valueEquals(value, JConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean valueEquals(String value, int timeoutInSeconds) throws Exception {
		return new WebDriverWait(driver, timeoutInSeconds).until(JExpectedConditions.valueEquals(this, value));
	}
	
	public boolean valueNotEquals(String value) throws Exception {
		return valueNotEquals(value, JConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean valueNotEquals(String value, int timeoutInSeconds) throws Exception {
		return new WebDriverWait(driver, timeoutInSeconds).until(JExpectedConditions.valueNotEquals(this, value));
	}
	
	public boolean isNotDisplayed() throws Exception {
		return isNotDisplayed(JConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean isNotDisplayed(int timeoutInSeconds) throws Exception{
		try {
			return new WebDriverWait(driver, timeoutInSeconds).until(JExpectedConditions.isNotDisplayed(this));
		}catch (TimeoutException e) {
			return false;
		}
	}
	
	public boolean isSelected() throws Exception {
		int timeOut = 3;
		return isSelected(timeOut);
	}
	
	public boolean isSelected(int timeOutInSeconds) throws Exception {
		assertDisplayed();
		
		try {
			return new WebDriverWait(driver, timeOutInSeconds).until(JExpectedConditions.isSelected(this));
		}catch (Exception e) {
			return false;
		}
	}
	
	public JElement input(String inputStr) throws Exception {
		return input(inputStr, inputStr);
	}
	
	public JElement input(String inputStr, String expectedValue) throws Exception {
		assertDisplayed();

		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				WebElement element = getWebElement();
				this.clear();
				element.sendKeys(inputStr);
				
				if(valueEquals(expectedValue)) {
					return this;
				}else {
					throw new Exception("Send keys failed. Input str: " + inputStr + ", expectedValue:" + expectedValue + ", Actual value:" + getValue());
				}
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	public int size() throws Exception{
		return getWebElements().size();
	}
	
	public void focus() throws Exception{	
		assertDisplayed();

		for(int i = 0; i< JConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				if(!isVisibleInViewport() && getWebElement().getSize().height<200) {
					JavascriptExecutor je = (JavascriptExecutor) driver;
			        je.executeScript("arguments[0].scrollIntoView({block: \"center\"});",getWebElement());
				}
				return;
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	private void assertDisplayed() throws Exception {
		if(!isDisplayed()) {
			throw new Exception("Element not found either not displayed. By List:" + this.byList);
		}
	}
	
	private void assertExists() throws Exception {
		if(!exists()) {
			throw new Exception("Element does not exist. By List:" + this.byList);
		}
	}
}
