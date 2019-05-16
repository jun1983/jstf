package com.jstf.main;

import com.jstf.config.ConfigHelper;
import com.jstf.config.QConfig;
import com.jstf.utils.QLogger;
import com.jstf.zap.ZAPProxy;

public class JSTF {
	public static void setup() throws Exception {
		QLogger.getLogger().info("**********************************************************************************************************");
		QLogger.getLogger().info("*******************************         JSTF  SETUP        ***********************************************");
		QLogger.getLogger().info("**********************************************************************************************************");
		
		//Check all settings if any conflicts
		doHealthCheck();
		
		//Start ZAP server if enabled
		if(QConfig.IS_ZAP_ENABLED) {
			QLogger.getLogger().info("ZAP security proxy is enabled.");
			String host = QConfig.ZAP_SERVER.split(":")[0];
			String port = QConfig.ZAP_SERVER.split(":")[1];
			if(host.equalsIgnoreCase("127.0.0.1")|| host.equalsIgnoreCase("localhost")) {
				ZAPProxy.start("127.0.0.1", Integer.parseInt(port));
			}else {
				ZAPProxy.connect(host, Integer.parseInt(port));
			}
		}
		
		if(QConfig.IS_MOCK_PROXY_ENABLED) {
			QLogger.getLogger().info("Mock Proxy is enabled.");
		}

		if(QConfig.IS_PROXY_ENABLED) {
			QLogger.getLogger().info("Proxy is used for testing");
			QLogger.getLogger().info("Proxy address: " + QConfig.PROXY_ADDR);
			QLogger.getLogger().info("Bypass: " + QConfig.PROXY_BYPASS);
		}
		
		QLogger.getLogger().info("Test Settings Info:");
		if(!QConfig.IS_REMOTE_DRIVER) {
			QLogger.getLogger().info("Using local browser - " + QConfig.BROWSER);
			QLogger.getLogger().info("Browser headless: " + (QConfig.IS_HEADLESS_BROWSER? "Yes":"No"));
		}else {
			QLogger.getLogger().info("Using remote browser.");
			QLogger.getLogger().info(ConfigHelper.getRemoteCapability().toString());
		}
		
		QLogger.getLogger().info("End of JSTF Steup.");
		QLogger.getLogger().info("**********************************************************************************************************");
		QLogger.getLogger().info("**********************************************************************************************************");
	}
	
	private static void doHealthCheck() throws Exception {
		QLogger.getLogger().info("Health checking...");

		//Check test configuration conflicts
		//Headless browser cannot be used with mock proxy
		if(QConfig.IS_HEADLESS_BROWSER && QConfig.IS_MOCK_PROXY_ENABLED) {
			QLogger.getLogger().error("Critical Error: Mock Proxy is not allowed in headless mode. To use headless browser, please disable mockproxy by changing IS_MOCK_PROXY_ENABLED.");
			System.exit(-9997);
		}
		
		//Headless Chrome does not support ZAP
		if(QConfig.IS_HEADLESS_BROWSER && QConfig.IS_ZAP_ENABLED) {
			QLogger.getLogger().error("Critical Error: Headless Chrome does not support ZAP. Please change browser to headed mode to use ZAP security testing.");
			System.exit(-9997);
		}
		
		if(QConfig.IS_MOCK_PROXY_ENABLED && QConfig.IS_REMOTE_DRIVER) {
			QLogger.getLogger().error("Critical Error: Mock Proxy is not allowed on remote browser. To use remote browser, please disable mockproxy by changing IS_MOCK_PROXY_ENABLED.");
			System.exit(-9987);
		}
		
		if(QConfig.IS_ZAP_ENABLED && QConfig.IS_REMOTE_DRIVER) {
			QLogger.getLogger().error("Critical Error: ZAP is not allowed on remote browser. To use remote browser, please disable ZAP in configuration.");
			System.exit(-9986);
		}
	}
	
	public static void teardown() throws Exception {
		QLogger.getLogger().info("**********************************************************************************************************");
		QLogger.getLogger().info("******************************         JSTF  TEARDOWN        *********************************************");
		QLogger.getLogger().info("**********************************************************************************************************");
		if(QConfig.IS_ZAP_ENABLED) {
			QLogger.getLogger().info("Tearing down ZAP Proxy..");
			ZAPProxy.stop();
		}
		QLogger.getLogger().info("*************************         JSTF  TEARDOWN  FINISHED       ****************************************");

	}
}
