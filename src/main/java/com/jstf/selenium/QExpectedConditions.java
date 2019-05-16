package com.jstf.selenium;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class QExpectedConditions{
	
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
	
	public static ExpectedCondition<Boolean> textContains(final QElement qElement, final String text) {
	    return new ExpectedCondition<Boolean>() {
		    String actualText = "";
	    		@Override
		      public Boolean apply(WebDriver driver) {
		        try {
		        		actualText = qElement.getText();
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
	
	public static ExpectedCondition<Boolean> textContainsIgnoreCase(final QElement qElement, final String text) {
		 return new ExpectedCondition<Boolean>() {
			    String actualText = "";
		    		@Override
			      public Boolean apply(WebDriver driver) {
			        try {
			        		actualText = qElement.getText();
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
	
	public static ExpectedCondition<Boolean> isDisplayed(final QElement qElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					List<WebElement> elements = qElement.getWebElements();
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
	
	public static ExpectedCondition<Boolean> exists(final QElement qElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return qElement.size()>0;
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> isEnabled(final QElement qElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return qElement.getWebElement().isEnabled();
				} catch (Exception e) {
					return false;
				}
			}
			
		   };
	}
	
	public static ExpectedCondition<Boolean> isNotDisplayed(final QElement qElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !qElement.getWebElement().isDisplayed();
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
	
	public static ExpectedCondition<Boolean> isSelected(final QElement qElement){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return qElement.getWebElement().isSelected()? true : false;
				} catch (Exception e) {
					return false;
				}
			}
			
		   };
	}
	
	public static ExpectedCondition<Boolean> attributeContains(final QElement qElement, String attribute, String value){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return qElement.getAttribute(attribute).contains(value);
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> attributeNotEquals(final QElement qElement, String attribute, String value){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !qElement.getAttribute(attribute).equals(value);
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> textEquals(final QElement qElement, final String text) {
	    return new ExpectedCondition<Boolean>() {
		      @Override
		      public Boolean apply(WebDriver driver) {
		        try {
					return qElement.getText().equals(text);
				} catch (Exception e) {
					return false;
				}
		      }

		      @Override
		      public String toString() {
		        try {
					return String.format("expected to have text \"%s\". Actual text: \"%s\"", text, qElement.getText());
				} catch (Exception e) {
					return e.getMessage();
				}
		      }
		};
	}
	
	public static ExpectedCondition<Boolean> valueEquals(final QElement qElement, final String value) {
	    return new ExpectedCondition<Boolean>() {
		      @Override
		      public Boolean apply(WebDriver driver) {
		        try {
					return qElement.getAttribute("value").equals(value);
				} catch (Exception e) {
					return false;
				}
		      }

		      @Override
		      public String toString() {
		        try {
					return String.format("expected to have value \"%s\". Actual text: \"%s\"", value, qElement.getAttribute("value"));
				} catch (Exception e) {
					return e.getMessage();
				}
		      }
		};
	}
	
	public static ExpectedCondition<Boolean> textNotEquals(final QElement qElement, String text){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !qElement.getText().equals(text);
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
	
	public static ExpectedCondition<Boolean> valueNotEquals(final QElement qElement, String value){
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !qElement.getAttribute("value").equals(value);
				} catch (Exception e) {
					return false;
				}
			}
		   };
	}
}
