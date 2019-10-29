package com.jstf.tests;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;

import com.jstf.selenium.JAssert;
import com.jstf.selenium.JDriver;


public class SignupTest extends BaseTest {
	@Before
	public void setUp() throws Exception {

		jDriver = new JDriver();
		jDriver.start();
		jAssert = new JAssert(jDriver);
		
	}

	@After
	public void tearDown() throws Exception {
		jDriver.close();
	}
	
	@org.junit.Test
	public void signupWithExistingAccount() throws Exception {
		jDriver.getUrl(homepageUrl, "GitHub");
				
		jDriver.find(".HeaderMenu--logged-out").find(By.linkText("Sign up")).click();
		
		jAssert.titleContains("Join GitHub");
	}
}
