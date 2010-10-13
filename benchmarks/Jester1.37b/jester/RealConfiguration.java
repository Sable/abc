package jester;

import java.io.*;
import java.util.Properties;

public class RealConfiguration implements Configuration {
	public static final String DEFAULT_CONFIGURATION_FILENAME = "jester.cfg";

	private Logger myLogger = new RealLogger();
	private Properties myProperties;
	
	public RealConfiguration(String configFileName, PrintStream errorStream) throws IOException {
		super();
		myProperties = new Properties();
		InputStream configPropertyFile = ClassLoader.getSystemResourceAsStream(configFileName);
		if (configPropertyFile == null) {
			configPropertyFile = getClass().getResourceAsStream(configFileName);
		}
		if (configPropertyFile != null) {
			myProperties.load(configPropertyFile);
		}else{
			errorStream.println("Warning - could not find "+DEFAULT_CONFIGURATION_FILENAME+" so using default configuration values.");
		}
	}
	
	public RealConfiguration(String configFileName) throws IOException {
		this(configFileName, System.err);
	}
	
	public String compilationCommand() {
		return stringProperty("compilationCommand", "javac");
	}
	private boolean isTrue(String value) {
		return value.toLowerCase().equals("true");
	}
	private boolean isTrueProperty(String propertyName, boolean defaultValue) {
		String propertyValue = myProperties.getProperty(propertyName);
		if(propertyValue==null){
			return defaultValue;
		}
		return isTrue(propertyValue);
	}
	public boolean shouldReportEagerly() {
		return isTrueProperty("shouldReportEagerly", false);
	}
	public String sourceFileExtension() {
		return stringProperty("sourceFileExtension", ".java");
	}
	private String stringProperty(String propertyName, String defaultValue) {
		String result = myProperties.getProperty(propertyName);
		if (result == null) {
			return defaultValue;
		}
		return result;
	}
	public String testRunningCommand() {
		return stringProperty("testRunningCommand", "java jester.TestRunnerImpl");
	}
	public String testsPassString() {
		return stringProperty("testsPassString", "PASSED");
	}
	public Logger getLogger() {
		return myLogger;
	}
	public String xmlReportFileName() {
		return stringProperty("xmlReportFileName", "jesterReport.xml");
	}
	
	public boolean closeUIOnFinish() {
		return isTrueProperty("closeUIOnFinish", true);
	}
}