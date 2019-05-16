package com.jstf.zap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;

import org.zaproxy.clientapi.core.ClientApi;

import com.jstf.config.QConfig;
import com.jstf.utils.QLogger;
import com.jstf.utils.OSType;
import com.jstf.utils.ZipUtils;

public class ZAPProxy {
	private static String host;
	private static int port;
	private static ClientApi clientApi;
	private static final String zapAppPath = QConfig.ZAP_APP_PATH;

	private static final String scriptFileName = QConfig.OS.equals(OSType.WINDOWS)? "zap.bat" : "zap.sh";
	private static final String officialBinary="https://github.com/zaproxy/zaproxy/releases/download/2.7.0/ZAP_2.7.0_Core.tar.gz";
	
	public static void start(String listenAddress, int listenPort) throws Exception {
		String scriptFile = zapAppPath + File.separator + scriptFileName;

		File f = new File(zapAppPath);
		
		//Extract ZAP if does not exist
		if (!f.exists()) {
			if(!QConfig.IS_ZAP_AUTO_INSTALL) {
				QLogger.getLogger().error("Critical Error: ZAP application is not found. Path: " + f.getAbsolutePath());
				QLogger.getLogger().error("Please enable zap_auto_install and download server in configuration. \nInstalltion file size is around 35MB and it takes a few minutes to finish the installation.");
				QLogger.getLogger().error("Alternatively you can install it manually and set zap_app_path to installation path.");
				QLogger.getLogger().error("Official binary can be downloaded from: " + officialBinary);
				System.exit(-50000);
			}else {
				QLogger.getLogger().warn("ZAP application is not found. Path: " + f.getAbsolutePath());
				QLogger.getLogger().info("Start to download ZAP application from:" + QConfig.ZAP_BINARY_DOWNLOAD);
				QLogger.getLogger().info("Installtion file size is around 35MB and it takes a few minutes to finish the installation.");
				
				
			}
		}
		
		//Add execution permission to ZAP script.
		if(!new File(scriptFile).canExecute()) {
			try {
				new File(scriptFile).setExecutable(true);
			}catch (SecurityException e) {
				e.printStackTrace();
				QLogger.getLogger().error("Add execution permission to ZAP script failed. ");
				System.exit(-43721);
			}
		}
		
		host = listenAddress;
		port = listenPort;
		
		ProcessBuilder pb;
		pb = new ProcessBuilder(scriptFile, "-daemon", "-host", "127.0.0.1", "-port", String.valueOf(port), "-config", "api.disablekey=true");
		
		File log = new File("target/logs/zap.log");
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.appendTo(log));
		pb.start();
		
		//Wait for ZAP started and connection check.
		connect(host, port);
		
		if(QConfig.IS_PROXY_ENABLED) {
			QLogger.getLogger().info("ZAP uses upchain proxy:" + QConfig.PROXY_ADDR);
			clientApi.core.setOptionUseProxyChain(true);
			clientApi.core.setOptionProxyChainName(QConfig.PROXY_ADDR.split(":")[0]);
			clientApi.core.setOptionProxyChainPort(Integer.parseInt(QConfig.PROXY_ADDR.split(":")[1]));
			clientApi.core.setOptionProxyChainSkipName(QConfig.PROXY_BYPASS);
		}else {
			clientApi.core.setOptionUseProxyChain(false);
			QLogger.getLogger().info("ZAP connects to internet directly.");
		}
		QLogger.getLogger().info("ZAP Server started successfully.");
	}
	
	public static ClientApi connect(String listenAddress, int listenPort) throws Exception {
		host = listenAddress;
		port = listenPort;
		
		clientApi = new ClientApi(host, port);
		clientApi.waitForSuccessfulConnectionToZap(30);
		return clientApi;
	}
	
	public static void stop() throws Exception{
		QLogger.getLogger().info("Generating ZAP report..");
		ZAPProxy.saveReport();
		if(clientApi!=null) {
			clientApi.core.shutdown();
			QLogger.getLogger().info("ZAP Server stopped successfully.");
		}else {
			QLogger.getLogger().warn("ZAP Server does not exist.");
		}
	}
	
	public static void saveReport() throws Exception {
		String reportPath = QConfig.ZAP_REPORT_PATH;
        FileOutputStream fos = new FileOutputStream(reportPath);
        fos.write(clientApi.core.htmlreport());
        fos.close();
        QLogger.getLogger().warn("ZAP report is saved at " + new File(reportPath).getAbsolutePath());
    }
	
	private static void installZAP(String binaryUrl) throws Exception {
		InputStream inputstream = new URL(binaryUrl).openStream();
		ZipUtils.unzip(inputstream, new File(zapAppPath));
	}
}
