package unittests;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.accessibility.JAXE;
import com.jstf.selenium.JDriver;
import com.jstf.selenium.JElement;

public class AxeTest extends BaseTest{
	private JDriver jDriver;
	
	@Before
	public void setUp() throws Exception {
		jDriver = new JDriver();
		jDriver.start();
	}

	@After
	public void tearDown() throws Exception {
		jDriver.closeMockProxy();
		jDriver.close();
	}	

	@org.junit.Test
	public void validatePage() throws Exception {
		jDriver.getUrl("https://www.google.com");
		new WebDriverWait(jDriver.getWebDriver(), 30).until(ExpectedConditions.titleIs("Google"));
		new JAXE(jDriver).validate();
	}
	
	@org.junit.Test
	public void validateNavElement() throws Exception {
		jDriver.getUrl("https://www.google.com");
		new WebDriverWait(jDriver.getWebDriver(), 30).until(ExpectedConditions.titleIs("Google"));
		JElement navElement = jDriver.find(By.id("gb"));
		new JAXE(jDriver).validate(navElement);
	}
}
