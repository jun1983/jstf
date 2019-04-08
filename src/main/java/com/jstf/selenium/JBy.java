package com.jstf.selenium;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByCssSelector;

public abstract class JBy extends By{
	/**
	   * @param linkText The exact text to match against
	   * @return a By which locates A elements by the exact text it displays
	   */
	  public static By classContains(final String className) {
	    if (className == null)
	      throw new IllegalArgumentException(
	          "Cannot find elements when className is null.");

	    return new ByClassContains(className);
	  }
	  
	  public static By ByTextEqualsIgnoreCase(By by, final String text) {
	    if (text == null)
	      throw new IllegalArgumentException(
	          "Cannot find elements when text is null.");

	    return new ByTextEqualsIgnoreCase(by, text);
	  }
	  
	  public static class ByTextEqualsIgnoreCase extends By implements Serializable{
		  	private static final long serialVersionUID = 1234567887654322L;
			private final String text;
			private final By by;
		    
		    public ByTextEqualsIgnoreCase(By by, String text) {
		    		this.by = by;
		    		this.text = text;
			}

		    @Override
		    public List<WebElement> findElements(SearchContext context) {
		    		List<WebElement> elements = new ArrayList<WebElement>();
		    		for (WebElement webElement : context.findElements(by)) {
						if(webElement.getText().equalsIgnoreCase(text)) {
							elements.add(webElement);
						}
					}
		    		return elements;
		    }

		    @Override
		    public WebElement findElement(SearchContext context) {
		    		List<WebElement> allElements = findElements(context);
		        if (allElements == null || allElements.isEmpty())
		          throw new NoSuchElementException("Cannot locate an element using "
		              + toString());
		        return allElements.get(0);
		    }
	  }
	  
	  public static By ByTextContainsIgnoreCase(By by, final String text) {
		    if (text == null)
		      throw new IllegalArgumentException(
		          "Cannot find elements when text is null.");

		    return new ByTextContainsIgnoreCase(by, text);
		  }
	  
	  public static By ByTextContainsIgnoreCase(String cssSelector, final String text) {
		    if (text == null)
		      throw new IllegalArgumentException(
		          "Cannot find elements when text is null.");

		    return new ByTextContainsIgnoreCase(cssSelector, text);
		  }
	  
	  public static class ByTextContainsIgnoreCase extends By implements Serializable{
		  	private static final long serialVersionUID = 1234567887654312L;
			private final String text;
			private final By by;
		    
		    public ByTextContainsIgnoreCase(By by, String text) {
		    		this.by = by;
		    		this.text = text;
			}
		    
		    public ByTextContainsIgnoreCase(String cssSelector, String text) {
	    		this.by = By.cssSelector(cssSelector);
	    		this.text = text;
		}

		    @Override
		    public List<WebElement> findElements(SearchContext context) {
		    		List<WebElement> elements = new ArrayList<WebElement>();
		    		for (WebElement webElement : context.findElements(by)) {
						if(StringUtils.containsIgnoreCase(webElement.getText(), text)) {
							elements.add(webElement);
						}
				}
		    		return elements;
		    }

		    @Override
		    public WebElement findElement(SearchContext context) {
		    		List<WebElement> allElements = findElements(context);
		        if (allElements == null || allElements.isEmpty())
		          throw new NoSuchElementException("Cannot locate an element using "
		              + toString());
		        return allElements.get(0);
		    }
	  }
	  
	  public static class ByClassContains extends By implements Serializable{
		  	private static final long serialVersionUID = 1234567887654321L;
			private final String className;

		    public ByClassContains(String className) {
		      this.className = className;
		    }
		    
		    @Override
		    public List<WebElement> findElements(SearchContext context) {
		      return ((FindsByCssSelector) context).findElementsByCssSelector("*[class*='"+className+"']");
		    }

		    @Override
		    public WebElement findElement(SearchContext context) {
		      return ((FindsByCssSelector) context).findElementByCssSelector("*[class*='"+className+"']");
		    }

		    @Override
		    public String toString() {
		      return "By.classContains: " + className;
		    }
		}
}


