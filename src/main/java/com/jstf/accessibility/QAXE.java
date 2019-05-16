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
import com.jstf.config.QConfig;
import com.jstf.selenium.QDriver;
import com.jstf.selenium.QElement;

public class QAXE {
	private final URL ruleScriptUrl = QAXE.class.getResource("/axe.min.js");
	private Builder builder;
	private WebDriver driver;
	
	private List<Rule> rules = new ArrayList<>();  //wcag2a, wcag2aa, wcag412,section508,section508.22.a, all
	private ReportLevel reportLevel;  //critical, serious, moderate, all
	
	public QAXE(WebDriver driver) throws Exception {
		List<String> axeRules = Arrays.asList(QConfig.AXE_RULES.toLowerCase().split(","));
		
		for (String axeRule : axeRules) {
			rules.add(Rule.fromString(axeRule));
		}
		this.reportLevel = ReportLevel.fromString(QConfig.AXE_REPORT_LEVEL.toLowerCase());
		
		this.driver = driver;
		init();
	}
		
	public QAXE(QDriver qDriver) throws Exception {
		this(qDriver.getDriver());
	}
	
	public QAXE(WebDriver driver, List<Rule> rules, ReportLevel reportLevel) throws JSONException {
		this.driver = driver;
		this.rules = rules;
		this.reportLevel = reportLevel;
		init();
	}
	
	public QAXE(QDriver qDriver, List<Rule> rules, ReportLevel reportLevel) throws JSONException {
		this(qDriver.getDriver(), rules, reportLevel);
	}
	
	private void init(){
		this.builder = new Builder(driver, ruleScriptUrl);
		if(rules != null && !rules.contains(Rule.ALL)) {
			JSONObject optionJson = new JSONObject();
			optionJson.put("runOnly", new JSONObject().put("type", "tag").put("values", rules));
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
	
	public void validate(QElement qElement) throws Exception {
		JSONObject jsonObject = builder.analyze(qElement.getWebElement());
		doAssertion(jsonObject);
	}
	
	private void doAssertion(JSONObject validationResult) throws Exception {
		validationResult = new JSONObject(validationResult.toString());
		
		JSONArray violations = validationResult.getJSONArray("violations");
		
		//Filter violations with severity
		JSONArray filteredViolations = new JSONArray();
		
		if(reportLevel==ReportLevel.ALL) {
			filteredViolations = violations;
		}else {
			JSONObject violation;
			String impact;
			for (int i=0; i<violations.length(); i++) {
				violation = violations.getJSONObject(i);
				impact = violation.getString("impact");
				ReportLevel violationLevel = ReportLevel.fromString(impact);
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

	public enum ReportLevel{
		CRITICAL, SERIOUS, MODERATE, ALL;
		
		public static ReportLevel fromString(String text) throws Exception{
	        switch (text.toLowerCase()) {
			case "critical":
				return ReportLevel.CRITICAL;
			case "serious":
				return ReportLevel.SERIOUS;
			case "moderate":
				return ReportLevel.MODERATE;
			case "all":
				return ReportLevel.ALL;
			default:
				throw new Exception(text + " is not supported.");
			}
		}
	}
	
	public enum Rule{
		WCAG2A, WCAG2AA, WCAG412, SECTION508, SECTION50822a, ALL;
		
		public static Rule fromString(String text) throws Exception{
	        switch (text.toLowerCase()) {
			case "wcag2a":
				return Rule.WCAG2A;
			case "wcag2aa":
				return Rule.WCAG2AA;
			case "wcag412":
				return Rule.WCAG412;
			case "section508":
				return Rule.SECTION508;
			case "section50822a":
				return Rule.SECTION50822a;
			case "all":
				return Rule.ALL;
			default:
				throw new Exception(text + " is not supported.");
			}
		}
	}
}
