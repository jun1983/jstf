package com.jstf.tests;

import com.jstf.selenium.JElement;


public class SignupTest extends BaseTest {
	
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
