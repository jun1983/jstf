package com.jstf.selenium;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.accessibility.QAXE;
import com.jstf.accessibility.QAXE.ReportLevel;
import com.jstf.accessibility.QAXE.Rule;
import com.jstf.config.QConfig;

import lombok.Cleanup;

public class QElement {
	private static final int retryInterval = 100;
	
	private List<QSelector> byList = new ArrayList<>();

	private QDriver qDriver;
	private WebDriver driver;
	
	private Exception resultException = null;

	
	public QElement(QDriver qDriver, By... bys) {
		this(qDriver, ElementSelectionType.ALL, bys);
	}
	
	public QElement(QElement qElement, String cssSelector) {
		this(qElement, By.cssSelector(cssSelector));
	}
	
	public QElement(QElement qElement, ElementSelectionType elementSelectionType, String cssSelector) {
		this(qElement, elementSelectionType, By.cssSelector(cssSelector));
	}
	
	public QElement(QElement qElement, By... bys) {
		this(qElement, ElementSelectionType.ALL, bys);
	}
	
	public QElement(QDriver qDriver, ElementSelectionType elementSelectionType, By... bys) {
		this.qDriver = qDriver;
		this.driver = qDriver.getDriver();
		
		for(int i=0;i<bys.length;i++) {
			if(i==bys.length-1) {
				byList.add(new QSelector(bys[i], elementSelectionType));
			}else {
				byList.add(new QSelector(bys[i], ElementSelectionType.ALL));
			}
		}
	}
	
	public QElement(QElement qElement, ElementSelectionType elementSelectionType, By... bys) {
		this.byList = new ArrayList<>(qElement.byList);
		this.qDriver = qElement.getQDriver();
		this.driver = qDriver.getDriver();
		
		for(int i=0;i<bys.length;i++) {
			if(i==bys.length-1) {
				byList.add(new QSelector(bys[i], elementSelectionType));
			}else {
				byList.add(new QSelector(bys[i], ElementSelectionType.ALL));
			}
		}
	}
	
	public QDriver getQDriver() {
		return this.qDriver;
	}
	
	public QElement find(By... bys) {
		return find(ElementSelectionType.ALL, bys);
	}
	
	public QElement find(ElementSelectionType elementSelectionType, By... bys) {
		return new QElement(this, elementSelectionType, bys);
	}
		
	public QElement find(String cssSelector) {
		return find(ElementSelectionType.ALL, cssSelector);
	}
	
	public QElement find(ElementSelectionType elementSelectionType, String cssSelector) {
		return new QElement(this, cssSelector);
	}
	
	public List<WebElement> getWebElements() throws Exception {
		@Cleanup QDriverWait qDriverWait = new QDriverWait(qDriver, 1);
		List<WebElement> elements = null;

		for (QSelector qBy : byList) {
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
	
	private List<WebElement> getSubElements(List<WebElement> elements, QSelector qBy){
		List<WebElement> results = new ArrayList<>();
		for (WebElement element : elements) {
			results.addAll(element.findElements(qBy.getBy()));
		}
		results = filterElementSelection(results, qBy.getElementSelectionType());
		return results;
	}
	
	public QElement parent() throws Exception{
		return find(By.xpath("./.."));
	}

	public QElement clear() throws Exception{
		isDisplayed();
		Exception resultException = null;
		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
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
	
	public QElement click() throws Exception {
		assertDisplayed();
		Exception resultException = null;
		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
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
	
	public QElement hover() throws Exception {
		return hover(null);
	}
	
	public QElement hover(ExpectedCondition<Boolean> expectedCondition) throws Exception {		
		focus();
		Exception resultException = null;
		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
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
		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
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
		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
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
		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
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
		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
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
		return isDisplayed(QConfig.ELEMENT_TIMEOUT);
	}
	
	public boolean isDisplayed(int timeoutInSeconds) throws Exception{
		try {
			return new WebDriverWait(driver, timeoutInSeconds).until(QExpectedConditions.isDisplayed(this));
		}catch (TimeoutException e) {
			return false;
		}
	}
	
	public boolean exists() throws Exception {
		return exists(QConfig.ELEMENT_TIMEOUT);
	}
	
	public boolean exists(int timeoutInSeconds) throws Exception{
		try {
			return new WebDriverWait(driver, timeoutInSeconds).until(QExpectedConditions.exists(this));
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
		return isEnabled(QConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean isEnabled(int timeoutInSeconds) throws Exception{
		try {
			return new WebDriverWait(driver, timeoutInSeconds).until(QExpectedConditions.isEnabled(this));
		}catch (TimeoutException e) {
			return false;
		}
	}
	
	public boolean textEquals(String text) throws Exception {
		return textEquals(text, QConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean textEquals(String text, int timeoutInSeconds) throws Exception {
		return new WebDriverWait(driver, timeoutInSeconds).until(QExpectedConditions.textEquals(this, text));
	}
	
	public boolean textNotEquals(String text) throws Exception {
		return textNotEquals(text, QConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean textNotEquals(String text, int timeoutInSeconds) throws Exception {
		return new WebDriverWait(driver, timeoutInSeconds).until(QExpectedConditions.textNotEquals(this, text));
	}
	
	public boolean valueEquals(String value) throws Exception {
		return valueEquals(value, QConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean valueEquals(String value, int timeoutInSeconds) throws Exception {
		return new WebDriverWait(driver, timeoutInSeconds).until(QExpectedConditions.valueEquals(this, value));
	}
	
	public boolean valueNotEquals(String value) throws Exception {
		return valueNotEquals(value, QConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean valueNotEquals(String value, int timeoutInSeconds) throws Exception {
		return new WebDriverWait(driver, timeoutInSeconds).until(QExpectedConditions.valueNotEquals(this, value));
	}
	
	public boolean isNotDisplayed() throws Exception {
		return isNotDisplayed(QConfig.ASSERTION_TIMEOUT);
	}
	
	public boolean isNotDisplayed(int timeoutInSeconds) throws Exception{
		try {
			return new WebDriverWait(driver, timeoutInSeconds).until(QExpectedConditions.isNotDisplayed(this));
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
			return new WebDriverWait(driver, timeOutInSeconds).until(QExpectedConditions.isSelected(this));
		}catch (Exception e) {
			return false;
		}
	}
	
	public QElement input(String inputStr) throws Exception {
		return input(inputStr, inputStr);
	}
	
	public QElement input(String inputStr, String expectedValue) throws Exception {
		assertDisplayed();

		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
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

		for(int i = 0; i< QConfig.ELEMENT_RETRYTIMES; i++) {
			try{
				if(!isVisibleInViewport() && getWebElement().getSize().height<200) {
					JavascriptExecutor je = (JavascriptExecutor) driver;
			        je.executeScript("arguments[0].scrollIntoView({block: \"center\"});",getWebElement());
			        if(!isPositioned()) {
						throw new Exception("Element is still moving.");
					}
				}
				return;
			}catch(Exception e) {
				resultException = e;
				Thread.sleep(retryInterval);
			}
		}
		throw resultException;
	}
	
	/**
	 * Check element is at a fixed position. This function is to avoid to click on a moving element.
	 * @return
	 */
	public boolean isPositioned() {
		return isPositioned(QConfig.ELEMENT_TIMEOUT);
	}
	
	/**
	 * Check element is at a fixed position. This function is to avoid to click on a moving element.
	 * @param timeOutInSeconds
	 * @return
	 */
	public boolean isPositioned(int timeOutInSeconds) {
		return new WebDriverWait(driver, timeOutInSeconds).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					Point elementPosition = getWebElement().getLocation();
					Thread.sleep(500);
					return getWebElement().getLocation().equals(elementPosition);
				}catch (Exception e) {
					return false;
				}
			}
		});
	}
	
	public void validateAccessibility() throws Exception {
		new QAXE(qDriver).validate(this);
	}
	
	public void validateAccessibility(List<Rule> rules, ReportLevel reportLevel) throws Exception {
		new QAXE(qDriver, rules, reportLevel).validate(this);
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
