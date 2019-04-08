package com.jstf.utils;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class JLogger {
	private static Logger logger;
	
	public static synchronized Logger getLogger(){
		if(logger!=null) {
			return logger;
		}
		
		Logger logger = (Logger) LoggerFactory.getLogger("jstf");
	    JLogger.logger = logger;
	    return logger;
	}
}
