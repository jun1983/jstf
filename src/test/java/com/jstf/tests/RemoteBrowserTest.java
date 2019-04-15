package com.jstf.tests;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.jstf.selenium.JAssert;
import com.jstf.selenium.JDriver;
import com.jstf.selenium.JElement;

public class RemoteBrowserTest extends BaseTest {
	@Before
	public void setUp() throws Exception {
		System.out.println("Start to create remote driver.");
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability("securityToken", "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzbFV4OFFBdjdVellIajd4YWstR0tTbE43UjFNSllDbC1TRVJiTlU1RFlFIn0.eyJqdGkiOiJlNmQ4MTM2YS04N2ViLTQ2NzQtODdiMC0wZTdlNmZkNDBjOWIiLCJleHAiOjAsIm5iZiI6MCwiaWF0IjoxNTU1MzE0NDQwLCJpc3MiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwiYXVkIjoib2ZmbGluZS10b2tlbi1nZW5lcmF0b3IiLCJzdWIiOiJjNTBmMWM2OC0yZmExLTQ1MTktYjFkOC01M2QzZjFmOWI3M2YiLCJ0eXAiOiJPZmZsaW5lIiwiYXpwIjoib2ZmbGluZS10b2tlbi1nZW5lcmF0b3IiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJiODJhOGU3Mi1mNWRjLTRiMjctYWZmZi05Mzc0YmMzYTJmOTIiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX19.fyIqLafM0x0Qs8UYrKNIy6ULBhrPagVPVpAKVJZqUpFpkk0RuxtLuf-IVWqQI_4Qwn8l9agdky05auE_ylX5syHklN7nmFl36woeVImyTK-FUZBF__nPJhVHpuTGYhYGts2JGDpv5qR-qDrOKGMPW8P4v1Tt9k8jCZVyQWdrPgAe5HqmILoAoE3abdFtJovVaEF8kfW5pkNEEX9-WTDhACjr4_Q550QXRwNQnXMFNVsCxdNQQjMaZZRWE8M_CCaG3y8z1MyOj-2yycHioSHKhFiZt1OPjDrDaxckCsJXZHKJ4CZnwmqVGagAffq1Ucw1p9rQCb_3dSEawY6IAA_Uag");
		capabilities.setCapability("platformName", "Windows");
		capabilities.setCapability("platformVersion", "10");
		capabilities.setCapability("browserName", "Chrome");
		capabilities.setCapability("browserVersion", "73");
		capabilities.setCapability("resolution", "1280x1024");
		capabilities.setCapability("location", "AP Sydney");
		capabilities.setCapability("scriptName", "Qantas-Windows-Test");
		jDriver = new JDriver(capabilities);
		jDriver.start();
		jAssert = new JAssert(jDriver);
		System.out.println("Remote driver created.");
	}

	@After
	public void tearDown() throws Exception {
		jDriver.close();
	}
	
	@org.junit.Test
	public void signupWithRemoteDriver() throws Exception {
		jDriver.getUrl(homepageUrl);
		
		jAssert.titleContains("GitHub");
		
		JElement signupSectionElement = jDriver.find(".home-hero-signup.text-gray-dark");

		signupSectionElement.find("*[id='user[login]']").input("JSTF");
		signupSectionElement.find("*[id='user[email]']").input("jun1983.sheng@gmail.com");
		signupSectionElement.find("*[id='user[password]']").input("ASsdfg#@#3");
		signupSectionElement.find("button").click();
		
		jAssert.titleContains("Join GitHub");
	}
}
