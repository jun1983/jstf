package com.jstf.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.jstf.utils.JLogger;

import ch.qos.logback.classic.Logger;

public class ConfigHelper {
	private static Logger logger = JLogger.getLogger();
	private static Map<String, String> jstfConfig = new HashMap<>();
	private static final String JSTF_CONFIG_FILE = "jstf_config_file";

	static {		
		String configFile = System.getenv(JSTF_CONFIG_FILE);
		if(configFile==null || configFile.isEmpty()) {
			configFile = "jconfig.yml";
		}
		
		try {
			jstfConfig = loadYamlFile(configFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-10011);
		}
	}
	
	/**
	 * 
	 * Parse the YAML file and return the output as a series of Maps and Lists
	 */
	public static Map<String, String> loadYamlFile(String filePath) throws IOException {
	    File ymlFile = new File(filePath);
	    
	    if(!ymlFile.exists()) {
	    		logger.error("Critical Error: JSTF config file is not found at " + ymlFile.getAbsolutePath());
	    		System.exit(-10010);
	    }
	    	
	    Yaml yaml = new Yaml();

	    InputStream ios = new FileInputStream(ymlFile);
        Map<String, String> yamlData = (Map<String, String>) yaml.load(ios);
        return yamlData;
	}
	
	public static String getConfig(String name) {
		if(hasBambooVariable(name)) {
			return getBambooVariable(name);
		}
		
		if(System.getenv("jstf_" + name)!=null) {
			return System.getenv("jstf_" + name);
		}
		
		if(jstfConfig.containsKey(name) && jstfConfig.get(name)!=null) {
			return jstfConfig.get(name);
		}else {
			return null;
		}
	}
	
	private static String getBambooVariable(String name) {
		return System.getenv("bamboo_" + name);
	}
	
	private static boolean hasBambooVariable(String name) {
		return getBambooVariable(name)!=null && !getBambooVariable(name).isEmpty();
	}
}
