package com.jstf.selenium;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class JExpectedConditions{
	
	public static ExpectedCondition<Boolean> titleContainsIgnoreCase(final String title) {
	    return new ExpectedCondition<Boolean>() {
	      private String currentTitle = "";
	      @Override
	      public Boolean apply(WebDriver driver) {
	        currentTitle = driver.getTitle();
	        return currentTitle.toLowerCase().contains(title.toLowerCase());
	      }

	      @Override
	      public String toString() {
	        return String.format("title to be \"%s\". Current title: \"%s\"", title, currentTitle);
	      }
	    };
	}
	
	public static ExpectedCondition<Boolean> titleContains(final String title) {
	    return new ExpectedCondition<Boolean>() {
	      private String currentTitle = "";
	      @Override
	      public Boolean apply(WebDriver driver) {
	        return driver.getTitle().contains(title);
	      }

	      @Override
	      public String toString() {
	        return String.format("title to be \"%s\". Current title: \"%s\"", title, currentTitle);
	      }
	    };
	}
	
	public static ExpectedCondition<Cookie> hasCookie(final String name) {
	    return new ExpectedCondition<Cookie>() {
	      @Override
	      public Cookie apply(WebDriver driver) {
	      	return driver.manage().getCookieNamed(name);
	      }

	      @Override
	      public String toString() {
	        return String.format("Failed to find \"%s\" in cookie", name);
	      }
	    };
	}
	
	public static ExpectedCondition<Boolean> textContains(final JElement je, final String text) {
	    return new ExpectedCondition<Boolean>() {
		    String actualText = "";
	    		@Override
		      public Boolean apply(WebDriver driver) {
		        try {
		        		actualText = je.getText();
					return actualText.contains(text);
				} catch (Exception e) {
					return false;
				}
		      }

		      @Override
		      public String toString() {
		        return String.format("expected to contain \"%s\". Actual text: \"%s\"", text, actualText);
		      }
		};
	}
	
	public static ExpectedCondition<Boolean> textContainsIgnoreCase(final JElement je, final String text) {
		 return new ExpectedCondition<Boolean>() {
			    String actualText = "";
		    		@Override
			      public Boolean apply(WebDriver driver) {
			        try {
			        		actualText = je.getText();
						return StringUtils.containsIgnoreCase(actualText, text);
					} catch (Exception e) {
						return false;
					}
			      }

			      @Override
			      public String toString() {
			        return String.format("expected to contain \"%s\". Actual text: \"%s\"", text, actualText);
			      }
			};
	}
	
	public static ExpectedCondition<Boolean> isDisplayed(final JElement jElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					List<WebElement> elements = jElement.getWebElements();
					if(elements.size()==0) {
						return false;
					}else {
						for (WebElement element : elements) {
							if(element.isDisplayed()) {
								return true;
							}
						}
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> exists(final JElement jElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return jElement.size()>0;
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> isEnabled(final JElement JElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return JElement.getWebElement().isEnabled();
				} catch (Exception e) {
					return false;
				}
			}
			
		   };
	}
	
	public static ExpectedCondition<Boolean> isNotDisplayed(final JElement JElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !JElement.getWebElement().isDisplayed();
				} catch (NotFoundException e) {
					return true;
				} catch (StaleElementReferenceException e) {
					return false;
				} catch (Exception e) {
					return true;
				}
			}
			
		   };
	}
	
	public static ExpectedCondition<Boolean> isSelected(final JElement jElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return jElement.getWebElement().isSelected()? true : false;
				} catch (Exception e) {
					return false;
				}
			}
			
		   };
	}
	
	public static ExpectedCondition<Boolean> attributeContains(final JElement jElement, String attribute, String value){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return jElement.getAttribute(attribute).contains(value);
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> attributeNotEquals(final JElement jElement, String attribute, String value){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !jElement.getAttribute(attribute).equals(value);
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> textEquals(final JElement jElement, final String text) {
	    return new ExpectedCondition<Boolean>() {
		      @Override
		      public Boolean apply(WebDriver driver) {
		        try {
					return jElement.getText().equals(text);
				} catch (Exception e) {
					return false;
				}
		      }

		      @Override
		      public String toString() {
		        try {
					return String.format("expected to have text \"%s\". Actual text: \"%s\"", text, jElement.getText());
				} catch (Exception e) {
					return e.getMessage();
				}
		      }
		};
	}
	
	public static ExpectedCondition<Boolean> valueEquals(final JElement jElement, final String value) {
	    return new ExpectedCondition<Boolean>() {
		      @Override
		      public Boolean apply(WebDriver driver) {
		        try {
					return jElement.getAttribute("value").equals(value);
				} catch (Exception e) {
					return false;
				}
		      }

		      @Override
		      public String toString() {
		        try {
					return String.format("expected to have value \"%s\". Actual text: \"%s\"", value, jElement.getAttribute("value"));
				} catch (Exception e) {
					return e.getMessage();
				}
		      }
		};
	}
	
	public static ExpectedCondition<Boolean> textNotEquals(final JElement jElement, String text){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !jElement.getText().equals(text);
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> valueNotEquals(final JElement jElement, String value){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !jElement.getAttribute("value").equals(value);
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
}
