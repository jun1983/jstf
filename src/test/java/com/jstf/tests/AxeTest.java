package com.jstf.tests;

import org.junit.After;
import org.junit.Before;

import com.jstf.accessibility.QAXE;
import com.jstf.selenium.QAssert;
import com.jstf.selenium.QDriver;
import com.jstf.selenium.QElement;

public class AxeTest extends BaseTest{
	@Before
	public void setUp() throws Exception {
		qDriver = new QDriver();
		qDriver.start();
		qAssert = new QAssert(qDriver);
		
	}

	@After
	public void tearDown() throws Exception {
		qDriver.closeMockProxy();
		qDriver.close();
	}

	@org.junit.Test
	public void validatePage() throws Exception {
		qDriver.getUrl(homepageUrl);
		qAssert.titleContains("GitHub");
		new QAXE(qDriver).validate();
	}
	
	@org.junit.Test
	public void validateNavElement() throws Exception {
		qDriver.getUrl(homepageUrl);
		qAssert.titleContains("GitHub");
		QElement navElement = qDriver.find(".HeaderMenu");
		new QAXE(qDriver).validate(navElement);
	}
}
