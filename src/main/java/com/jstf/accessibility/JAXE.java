package com.jstf.accessibility;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.deque.axe.AXE;
import com.deque.axe.AXE.Builder;
import com.jstf.config.JConfig;
import com.jstf.selenium.JDriver;
import com.jstf.selenium.JElement;

public class JAXE {
	private final URL ruleScriptUrl = JAXE.class.getResource("/axe.min.js");
	private Builder builder;
	private WebDriver driver;
	
	private List<AxeTag> tags = new ArrayList<>();  //wcag2a, wcag2aa, wcag412,section508,section508.22.a, all
	private AxeReportLevel reportLevel;  //critical, serious, moderate, all
	
	public JAXE(WebDriver driver) throws Exception {
		List<String> axeTags = Arrays.asList(JConfig.AXE_TAGS.toLowerCase().split(","));
		
		for (String axeTag : axeTags) {
			tags.add(AxeTag.fromString(axeTag));
		}
		this.reportLevel = AxeReportLevel.fromString(JConfig.AXE_REPORT_LEVEL.toLowerCase());
		
		this.driver = driver;
		init();
	}
		
	public JAXE(JDriver jDriver) throws Exception {
		this(jDriver.getDriver());
	}
	
	public JAXE(WebDriver driver, AxeReportLevel reportLevel, AxeTag...tags) throws JSONException {
		this.driver = driver;
		this.tags = Arrays.asList(tags);
		this.reportLevel = reportLevel;
		init();
	}
	
	public JAXE(JDriver jDriver, AxeReportLevel reportLevel, AxeTag...tags) throws JSONException {
		this(jDriver.getDriver(), reportLevel, tags);
	}
	
	private void init(){
		this.builder = new Builder(driver, ruleScriptUrl);
		if(tags != null && !tags.contains(AxeTag.ALL)) {
			
			List<String> axeRules = new ArrayList<>();
			for(int i=0;i<tags.size();i++) {
				axeRules.add(tags.get(i).toString());
			}
			
			JSONObject optionJson = new JSONObject();
			optionJson.put("runOnly", new JSONObject().put("type", "tag").put("values", axeRules));
			builder.options(optionJson.toString());
		}
	}
	
	public void validate() throws Exception {
		JSONObject jsonObject = builder.analyze();
		doAssertion(jsonObject);
	}
	
	public void validate(WebElement element) throws Exception {
		JSONObject jsonObject = builder.analyze(element);
		doAssertion(jsonObject);
	}
	
	public void validate(JElement jElement) throws Exception {
		JSONObject jsonObject = builder.analyze(jElement.getWebElement());
		doAssertion(jsonObject);
	}
	
	private void doAssertion(JSONObject validationResult) throws Exception {
		validationResult = new JSONObject(validationResult.toString());
		
		JSONArray violations = validationResult.getJSONArray("violations");
		
		//Filter violations with severity
		JSONArray filteredViolations = new JSONArray();
		
		if(reportLevel==AxeReportLevel.ALL) {
			filteredViolations = violations;
		}else {
			JSONObject violation;
			String impact;
			for (int i=0; i<violations.length(); i++) {
				violation = violations.getJSONObject(i);
				impact = violation.getString("impact");
				AxeReportLevel violationLevel = AxeReportLevel.fromString(impact);
				if(violationLevel.compareTo(this.reportLevel)<=0) {
					filteredViolations.put(violation);
				}
			}
		}
		
		if (filteredViolations.length() == 0) {
			assertTrue("No violations found", true);
		} else {
			assertTrue(AXE.report(filteredViolations), false);
		}
	}

	public enum AxeReportLevel{
		CRITICAL, SERIOUS, MODERATE, ALL;
		
		public static AxeReportLevel fromString(String text) throws Exception{
	        switch (text.toLowerCase()) {
			case "critical":
				return AxeReportLevel.CRITICAL;
			case "serious":
				return AxeReportLevel.SERIOUS;
			case "moderate":
				return AxeReportLevel.MODERATE;
			case "all":
				return AxeReportLevel.ALL;
			default:
				throw new Exception(text + " is not supported.");
			}
		}
	}
	
	public enum AxeTag{
		WCAG2A("wcag2a"), WCAG2AA("wcag2aa"), WCAG412("wcag412"), SECTION508("section508"), SECTION50822a("section50822a"), ALL("all");
		
		private final String text;
		
		private AxeTag(final String text) {
			this.text = text;
		}
		
		public String toString() {
			return this.text;
		}
		
		public static AxeTag fromString(String text) throws Exception{
	        switch (text.toLowerCase()) {
			case "wcag2a":
				return AxeTag.WCAG2A;
			case "wcag2aa":
				return AxeTag.WCAG2AA;
			case "wcag412":
				return AxeTag.WCAG412;
			case "section508":
				return AxeTag.SECTION508;
			case "section50822a":
				return AxeTag.SECTION50822a;
			case "all":
				return AxeTag.ALL;
			default:
				throw new Exception(text + " is not supported.");
			}
		}
	}
}
