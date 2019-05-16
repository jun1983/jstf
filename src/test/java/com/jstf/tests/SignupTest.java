package com.jstf.tests;

import org.junit.After;
import org.junit.Before;

import com.jstf.selenium.QAssert;
import com.jstf.selenium.QDriver;
import com.jstf.selenium.QElement;


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
		qDriver.getUrl(homepageUrl);
		
		qAssert.titleContains("GitHub");
		
		QElement signupSectionElement = qDriver.find(".home-hero-signup.text-gray-dark");

		signupSectionElement.find("*[id='user[login]']").input("JSTF");
		signupSectionElement.find("*[id='user[email]']").input("jun1983.sheng@gmail.com");
		signupSectionElement.find("*[id='user[password]']").input("ASsdfg#@#3");
		signupSectionElement.find("button").click();
		
		qAssert.titleContains("Join GitHub");
	}
}
