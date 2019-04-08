package jstf;

import org.junit.After;
import org.junit.Before;

import com.jstf.selenium.JDriver;

public class Test {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@org.junit.Test
	public void test() throws Exception {
		JDriver jDriver = new JDriver();
		jDriver.start();
	}

}
