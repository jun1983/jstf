package com.jstf.tests;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.selenium.JAssert;
import com.jstf.selenium.JDriver;

public class RemoteBrowserTest {
	private final static String homePage = "https://www.github.com";
	private WebDriver driver;
	private JDriver jDriver;
	
	@org.junit.Test
	public void signupWithLocalDriver() throws Exception {
		driver = createLocalDriver();
		testGitHubRegistrationWithNativeDriver();
	}
	
	@org.junit.Test
	public void signupWithPerfecto() throws Exception {		
		driver = createPerfectoDriver();
		testGitHubRegistrationWithNativeDriver();
	}
	
	@org.junit.Test
	public void signupWithBrowserStack() throws Exception {
		driver = createBrowserStackDriver();
		
		testGitHubRegistrationWithNativeDriver();
	}
	
	@org.junit.Test
	public void signupWithLocalJDriver() throws Exception {
		driver = createLocalDriver();
		jDriver = new JDriver();
		jDriver.setWebDriver(driver);
		testGitHubRegistrationWithJDriver();
	}
	
	@org.junit.Test
	public void signupWithPerfectoJDriver() throws Exception {
		driver = createPerfectoDriver();
		jDriver = new JDriver();
		jDriver.setWebDriver(driver);
		testGitHubRegistrationWithJDriver();
	}
	
	@org.junit.Test
	public void signupWithBrowserStackJDriver() throws Exception {
		driver = createBrowserStackDriver();
		jDriver = new JDriver();
		jDriver.setWebDriver(driver);
		testGitHubRegistrationWithJDriver();
	}
	
	private void testGitHubRegistrationWithNativeDriver() {
		driver.get(homePage);
		
		new WebDriverWait(driver, 30).until(ExpectedConditions.titleContains("GitHub"));
		
		driver.findElement(By.cssSelector(".home-hero-signup.text-gray-dark *[id='user[login]']")).sendKeys("Perfecto");
		driver.findElement(By.cssSelector(".home-hero-signup.text-gray-dark *[id='user[email]']")).sendKeys("a@b.c");
		driver.findElement(By.cssSelector(".home-hero-signup.text-gray-dark *[id='user[password]']")).sendKeys("ASsdfg#@#3");
		new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector(".home-hero-signup.text-gray-dark button"))).click();
		
		new WebDriverWait(driver, 30).until(ExpectedConditions.titleContains("Join GitHub"));
		driver.quit();
	}
	
	private void testGitHubRegistrationWithJDriver() throws Exception {
		jDriver.getUrl(homePage, "GitHub");
		jDriver.find(".home-hero-signup.text-gray-dark").find("*[id='user[login]']").input("Perfecto");
		jDriver.find(".home-hero-signup.text-gray-dark").find("*[id='user[email]']").input("a@b.c");
		jDriver.find(".home-hero-signup.text-gray-dark").find("*[id='user[password]']").input("ASsdfg#@#3");
		jDriver.find(".home-hero-signup.text-gray-dark").find("button").click();
		
		new JAssert(jDriver).titleContains("Join GitHub");
		jDriver.close();
	}
	
	private WebDriver createLocalDriver() {
		System.setProperty("webdriver.chrome.driver", "drivers/chromedriver");
		return new ChromeDriver();
	}
	
	private WebDriver createPerfectoDriver() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("securityToken", "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzbFV4OFFBdjdVellIajd4YWstR0tTbE43UjFNSllDbC1TRVJiTlU1RFlFIn0.eyJqdGkiOiJlNmQ4MTM2YS04N2ViLTQ2NzQtODdiMC0wZTdlNmZkNDBjOWIiLCJleHAiOjAsIm5iZiI6MCwiaWF0IjoxNTU1MzE0NDQwLCJpc3MiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwiYXVkIjoib2ZmbGluZS10b2tlbi1nZW5lcmF0b3IiLCJzdWIiOiJjNTBmMWM2OC0yZmExLTQ1MTktYjFkOC01M2QzZjFmOWI3M2YiLCJ0eXAiOiJPZmZsaW5lIiwiYXpwIjoib2ZmbGluZS10b2tlbi1nZW5lcmF0b3IiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJiODJhOGU3Mi1mNWRjLTRiMjctYWZmZi05Mzc0YmMzYTJmOTIiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX19.fyIqLafM0x0Qs8UYrKNIy6ULBhrPagVPVpAKVJZqUpFpkk0RuxtLuf-IVWqQI_4Qwn8l9agdky05auE_ylX5syHklN7nmFl36woeVImyTK-FUZBF__nPJhVHpuTGYhYGts2JGDpv5qR-qDrOKGMPW8P4v1Tt9k8jCZVyQWdrPgAe5HqmILoAoE3abdFtJovVaEF8kfW5pkNEEX9-WTDhACjr4_Q550QXRwNQnXMFNVsCxdNQQjMaZZRWE8M_CCaG3y8z1MyOj-2yycHioSHKhFiZt1OPjDrDaxckCsJXZHKJ4CZnwmqVGagAffq1Ucw1p9rQCb_3dSEawY6IAA_Uag");
		capabilities.setCapability("platformName", "Windows");
		capabilities.setCapability("platformVersion", "10");
		capabilities.setCapability("browserName", "Chrome");
		capabilities.setCapability("browserVersion", "73");
		capabilities.setCapability("resolution", "1280x1024");
		capabilities.setCapability("location", "AP Sydney");
		capabilities.setCapability("scriptName", "Qantas-Windows-Test");
		
		return new RemoteWebDriver(new URL("http://demo.perfectomobile.com/nexperience/perfectomobile/wd/hub/fast"), capabilities);
	}
	
	private WebDriver createBrowserStackDriver() throws MalformedURLException {
		final String hubUrl = "https://junsheng1:XZc9CPk6xDxpzcvn9jpu@hub-cloud.browserstack.com/wd/hub";
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("browser", "Chrome");
		capabilities.setCapability("browser_version", "73.0");
		capabilities.setCapability("os", "Windows");
		capabilities.setCapability("os_version", "10");
		capabilities.setCapability("resolution", "1280x1024");
		
		return new RemoteWebDriver(new URL(hubUrl), capabilities);
	}
	
}
