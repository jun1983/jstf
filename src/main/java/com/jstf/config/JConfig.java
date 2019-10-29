package com.jstf.config;

import static com.jstf.config.ConfigHelper.getConfig;

import org.apache.commons.lang3.StringUtils;

import com.jstf.utils.OSType;

public class JConfig {
	
	public static final String BROWSER = getConfig("browser")== null? "chrome" : getConfig("browser");
	public static final boolean IS_REMOTE_DRIVER = getConfig("is_remote_driver")== null? false : getConfig("is_remote_driver").equals("true");
	public static final String SELENIUM_HUB = getConfig("selenium_hub") == null? "" : getConfig("selenium_hub");
	public static final String REMOTE_DRIVER_CAPABILITY = getConfig("remote_driver_capability") == null? "" : getConfig("remote_driver_capability");
	public static final boolean IS_HEADLESS_BROWSER = getConfig("is_headless_browser")==null? false : getConfig("is_headless_browser").equals("true");
	
	//Mock Proxy Settings
	public static final boolean IS_MOCK_PROXY_ENABLED = getConfig("is_mock_proxy_enabled")==null? false:getConfig("is_mock_proxy_enabled").equals("true");
	public static final boolean IS_CORS_ENABLED = getConfig("is_cors_enabled")==null? false : getConfig("is_cors_enabled").equals("true");

	//ZAP Settings
	public static final boolean IS_ZAP_ENABLED = getConfig("is_zap_enabled")==null? false : getConfig("is_zap_enabled").equals("true");
	public static final String ZAP_SERVER = getConfig("zap_server")==null? "" : getConfig("zap_server");
	public static final String ZAP_APP_PATH = getConfig("zap_app_path") == null? "tools/ZAP_2.7.0" : StringUtils.strip(getConfig("zap_app_path"), "/");
	public static final String ZAP_REPORT_PATH = getConfig("zap_report_path")==null? "target/ZAPReport.html" : getConfig("zap_report_path");
	public static final String ZAP_LOG_FILE = getConfig("zap_log_file")==null? "target/jstflogs/zap.log" : getConfig("zap_log_file");
	public static final String ZAP_BINARY_DOWNLOAD = getConfig("zap_binary_download")==null? "https://github.com/zaproxy/zaproxy/releases/download/2.7.0/ZAP_2.7.0_Core.tar.gz" : getConfig("zap_binary_download");
	public static final boolean IS_ZAP_AUTO_INSTALL = getConfig("is_zap_auto_install")==null? false : getConfig("is_zap_auto_install").equals("true");
	
	public static final boolean IS_PROXY_ENABLED = getConfig("is_proxy_enabled")==null? false : getConfig("is_proxy_enabled").equals("true");
	public static final String PROXY_ADDR = getConfig("proxy_addr")==null? "" : getConfig("proxy_addr");
	public static final String PROXY_BYPASS = getConfig("proxy_bypass")==null? "" : getConfig("proxy_bypass");
	
	//Selenium config
	public static int ELEMENT_TIMEOUT = getConfig("element_timeout")==null? 30 : Integer.parseInt(getConfig("element_timeout"));
	public static int ASSERTION_TIMEOUT = getConfig("assertion_timeout") == null? 10 : Integer.parseInt(getConfig("assertion_timeout"));
	public static int ELEMENT_RETRYTIMES = getConfig("element_retrytimes")==null? 10 : Integer.parseInt(getConfig("element_retrytimes"));
	public static String DEFAULT_WINDOW_SIZE = getConfig("default_window_size")==null? "" : getConfig("default_window_size");

	//Accessibility config (AXE)
	public static String AXE_REPORT_LEVEL = getConfig("axe_report_level")==null? "serious" : getConfig("axe_report_level");
	public static String AXE_TAGS = getConfig("axe_tags")==null? "all" : getConfig("axe_tags");
	
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
