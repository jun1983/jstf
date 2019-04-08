package unittests;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.jstf.accessibility.JAXE;
import com.jstf.selenium.JElement;

public class AxeTest extends BaseTest{
	

	@org.junit.Test
	public void validatePage() throws Exception {
		jDriver.getUrl(homepageUrl);
		new WebDriverWait(jDriver.getWebDriver(), 30).until(ExpectedConditions.titleContains("GitHub"));
		new JAXE(jDriver).validate();
	}
	
	@org.junit.Test
	public void validateNavElement() throws Exception {
		jDriver.getUrl(homepageUrl);
		new WebDriverWait(jDriver.getWebDriver(), 30).until(ExpectedConditions.titleContains("GitHub"));
		JElement navElement = jDriver.find(".HeaderMenu");
		new JAXE(jDriver).validate(navElement);
	}
}
