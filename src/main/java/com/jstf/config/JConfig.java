package com.jstf.config;

import static com.jstf.config.ConfigHelper.getConfig;

import com.jstf.utils.OSType;

public class JConfig {
	
	public static final String BROWSER = getConfig("browser");
	public static final boolean IS_HEADLESS_BROWSER = getConfig("is_headless_browser").equals("true");
	
	public static final boolean IS_MOCK_PROXY_ENABLED = getConfig("is_mock_proxy_enabled").equals("true");

	public static final boolean IS_ZAP_ENABLED = getConfig("is_zap_enabled").equals("true");
	public static final String ZAP_SERVER = getConfig("zap_server");

	public static final boolean IS_PROXY_ENABLED = getConfig("is_proxy_enabled").equals("true");
	public static final String PROXY_ADDR = getConfig("proxy_addr");
	public static final String PROXY_BYPASS = getConfig("proxy_bypass");
	
	//Selenium config
	public static int ELEMENT_TIMEOUT = Integer.parseInt(getConfig("element_timeout"));
	public static int ASSERTION_TIMEOUT = Integer.parseInt(getConfig("assertion_timeout"));
	public static int ELEMENT_RETRYTIMES = Integer.parseInt(getConfig("element_retrytimes"));
	

	public static OSType OS;
	static {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
			OS = OSType.LINUX;
		 }else if(osName.indexOf("win") >= 0){
			 OS = OSType.WINDOWS;
		 }else if(osName.indexOf("mac") >= 0){
			 OS = OSType.MAC;
		 }
	}
}
