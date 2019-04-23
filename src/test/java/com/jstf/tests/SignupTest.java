package com.jstf.tests;

import org.junit.After;
import org.junit.Before;

import com.jstf.config.JConfig;
import com.jstf.selenium.JAssert;
import com.jstf.selenium.JDriver;
import com.jstf.selenium.JElement;


public class SignupTest extends BaseTest {
	@Before
	public void setUp() throws Exception {
		System.out.println(JConfig.BROWSER);
		System.out.println(JConfig.REMOTE_DRIVER_CAPABILITY);

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
