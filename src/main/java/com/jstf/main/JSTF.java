package com.jstf.main;

import com.jstf.config.JConfig;
import com.jstf.selenium.BrowserType;
import com.jstf.utils.JLogger;
import com.jstf.zap.ZAPProxy;

public class JSTF {
	public static void setup() throws Exception {
		JLogger.getLogger().info("**********************************************************************************************************");
		JLogger.getLogger().info("*******************************         JSTF  SETUP        ***********************************************");
		JLogger.getLogger().info("**********************************************************************************************************");
		
		//Check all settings if any conflicts
		doHealthCheck();
		
		//Start ZAP server if enabled
		if(JConfig.IS_ZAP_ENABLED) {
			JLogger.getLogger().info("ZAP security proxy is enabled.");
			String host = JConfig.ZAP_SERVER.split(":")[0];
			String port = JConfig.ZAP_SERVER.split(":")[1];
			if(host.equalsIgnoreCase("127.0.0.1")|| host.equalsIgnoreCase("localhost")) {
				ZAPProxy.start("127.0.0.1", Integer.parseInt(port));
			}else {
				ZAPProxy.connect(host, Integer.parseInt(port));
			}
		}
		
		if(JConfig.IS_MOCK_PROXY_ENABLED) {
			JLogger.getLogger().info("Mock Proxy is enabled.");
		}

		if(JConfig.IS_PROXY_ENABLED) {
			JLogger.getLogger().info("Proxy is used for testing");
			JLogger.getLogger().info("Proxy address: " + JConfig.PROXY_ADDR);
			JLogger.getLogger().info("Bypass: " + JConfig.PROXY_BYPASS);
		}
		
		JLogger.getLogger().info("Test Settings Info:");
		if(!BrowserType.fromString(JConfig.BROWSER).equals(BrowserType.REMOTE)) {
			JLogger.getLogger().info("Using local browser - " + JConfig.BROWSER);
			JLogger.getLogger().info("Browser headless: " + (JConfig.IS_HEADLESS_BROWSER? "Yes":"No"));
		}else {
			JLogger.getLogger().info("Using remote browser.");
			//JLogger.getLogger().info(Capabilities);
		}
		
		JLogger.getLogger().info("End of JSTF Steup.");
		JLogger.getLogger().info("**********************************************************************************************************");
		JLogger.getLogger().info("**********************************************************************************************************");
	}
	
	private static void doHealthCheck() throws Exception {
		JLogger.getLogger().info("Health checking...");

		//Check test configuration conflicts
		//Headless browser cannot be used with mock proxy
		if(JConfig.IS_HEADLESS_BROWSER && JConfig.IS_MOCK_PROXY_ENABLED) {
			JLogger.getLogger().error("Critical Error: Mock Proxy is not allowed in headless mode. To use headless browser, please disable mockproxy by changing IS_MOCK_PROXY_ENABLED.");
			System.exit(-9997);
		}
		
		//Headless Chrome does not support ZAP
		if(JConfig.IS_HEADLESS_BROWSER && JConfig.IS_ZAP_ENABLED) {
			JLogger.getLogger().error("Critical Error: Headless Chrome does not support ZAP. Please change browser to headed mode to use ZAP security testing.");
			System.exit(-9997);
		}
	}
	
	public static void teardown() throws Exception {
		JLogger.getLogger().info("**********************************************************************************************************");
		JLogger.getLogger().info("******************************         JSTF  TEARDOWN        *********************************************");
		JLogger.getLogger().info("**********************************************************************************************************");
		if(JConfig.IS_ZAP_ENABLED) {
			JLogger.getLogger().info("Tearing down ZAP Proxy..");
			ZAPProxy.stop();
		}
		JLogger.getLogger().info("*************************         JSTF  TEARDOWN  FINISHED       ****************************************");

	}
}
