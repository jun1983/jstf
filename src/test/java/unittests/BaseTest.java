package unittests;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.jstf.main.JSTF;

public abstract class BaseTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JSTF.setup();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

}
