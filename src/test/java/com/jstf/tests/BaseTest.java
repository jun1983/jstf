package com.jstf.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.jstf.main.JSTF;
import com.jstf.selenium.QAssert;
import com.jstf.selenium.QDriver;

public abstract class BaseTest {
	protected final static String homepageUrl = "https://github.com/";
	protected QDriver qDriver;
	protected QAssert qAssert;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JSTF.setup();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		JSTF.teardown();
	}
	
	
}
