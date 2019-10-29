package com.jstf.tests;

import org.junit.After;
import org.junit.Before;

import com.jstf.accessibility.JAXE;
import com.jstf.selenium.JAssert;
import com.jstf.selenium.JDriver;
import com.jstf.selenium.JElement;

public class AxeTest extends BaseTest{
	@Before
	public void setUp() throws Exception {
		jDriver = new JDriver();
		jDriver.start();
		jAssert = new JAssert(jDriver);
		
	}

	@After
	public void tearDown() throws Exception {
		jDriver.closeMockProxy();
		jDriver.close();
	}

	@org.junit.Test
	public void validatePage() throws Exception {
		jDriver.getUrl(homepageUrl);
		jAssert.titleContains("GitHub");
		new JAXE(jDriver).validate();
	}
	
	@org.junit.Test
	public void validateNavElement() throws Exception {
		jDriver.getUrl(homepageUrl);
		jAssert.titleContains("GitHub");
		JElement navElement = jDriver.find(".HeaderMenu");
		new JAXE(jDriver).validate(navElement);
	}
}
