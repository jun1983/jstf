package com.jstf.tests;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;

import com.jstf.selenium.QAssert;
import com.jstf.selenium.QDriver;


public class SignupTest extends BaseTest {
	@Before
	public void setUp() throws Exception {

		qDriver = new QDriver();
		qDriver.start();
		qAssert = new QAssert(qDriver);
		
	}

	@After
	public void tearDown() throws Exception {
		qDriver.close();
	}
	
	@org.junit.Test
	public void signupWithExistingAccount() throws Exception {
		qDriver.getUrl(homepageUrl, "GitHub");
				
		qDriver.find(".HeaderMenu--logged-out").find(By.linkText("Sign up")).click();
		
		qAssert.titleContains("Join GitHub");
	}
}
