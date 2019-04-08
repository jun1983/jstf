package com.jstf.zap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.zaproxy.clientapi.core.ClientApi;

import com.jstf.config.JConfig;
import com.jstf.utils.JLogger;
import com.jstf.utils.OSType;
import com.jstf.utils.ZipUtils;

public class ZAPProxy {
	private static String host;
	private static int port;
	private static ClientApi clientApi;
	private static String tmpZAPInstallPath = "target/tools";
	
	public static void start(String listenAddress, int listenPort) throws Exception {
		String zapAppPath = JConfig.ZAP_APP_PATH;
		String scriptFile=zapAppPath;

		File f = new File(zapAppPath);
		//Extract ZAP if does not exist
		if (!f.exists() || f.isDirectory()) {
			JLogger.getLogger().error("Critical Error: ZAP application is not found. Path: " + f.getAbsolutePath());
			System.exit(-50000);
		}
		
		String fileExtension = f.getName().substring(f.getName().lastIndexOf("."));;
		
		if(fileExtension.equals(".zip")) {
			JLogger.getLogger().info("ZAP ZIP is found at:" + f.getAbsolutePath());
			JLogger.getLogger().info("Unzip ZAP to:" + new File(tmpZAPInstallPath).getAbsolutePath());
			unzipZAP(f);
			if(JConfig.OS.equals(OSType.WINDOWS)) {
				scriptFile = tmpZAPInstallPath + "/ZAP_2.7.0/zap.bat";
			}else {
				scriptFile = tmpZAPInstallPath + "/ZAP_2.7.0/zap.sh";
				//add permission
			   Set<PosixFilePermission> permission = PosixFilePermissions.fromString("rwxr-xr-x");
			   Files.setPosixFilePermissions(new File(scriptFile).toPath(), permission);
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
		
		if(JConfig.IS_PROXY_ENABLED) {
			JLogger.getLogger().info("ZAP uses upchain proxy:" + JConfig.PROXY_ADDR);
			clientApi.core.setOptionUseProxyChain(true);
			clientApi.core.setOptionProxyChainName(JConfig.PROXY_ADDR.split(":")[0]);
			clientApi.core.setOptionProxyChainPort(Integer.parseInt(JConfig.PROXY_ADDR.split(":")[1]));
			clientApi.core.setOptionProxyChainSkipName(JConfig.PROXY_BYPASS);
		}else {
			clientApi.core.setOptionUseProxyChain(false);
			JLogger.getLogger().info("ZAP connects to internet directly.");
		}
		JLogger.getLogger().info("ZAP Server started successfully.");
	}
	
	public static ClientApi connect(String listenAddress, int listenPort) throws Exception {
		host = listenAddress;
		port = listenPort;
		
		clientApi = new ClientApi(host, port);
		clientApi.waitForSuccessfulConnectionToZap(30);
		return clientApi;
	}
	
	public static void stop() throws Exception{
		JLogger.getLogger().info("Generating ZAP report..");
		ZAPProxy.saveReport();
		if(clientApi!=null) {
			clientApi.core.shutdown();
			JLogger.getLogger().info("ZAP Server stopped successfully.");
		}else {
			JLogger.getLogger().warn("ZAP Server does not exist.");
		}
	}
	
	public static void saveReport() throws Exception {
		String reportPath = JConfig.ZAP_REPORT_PATH;
        FileOutputStream fos = new FileOutputStream(reportPath);
        fos.write(clientApi.core.htmlreport());
        fos.close();
        JLogger.getLogger().warn("ZAP report is saved at " + new File(reportPath).getAbsolutePath());
    }
	
	private static void unzipZAP(File file) throws Exception {
		InputStream inputstream = new FileInputStream(file);
		ZipUtils.unzip(inputstream, new File(tmpZAPInstallPath));
	}
}
