package jester;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class RealLogger implements Logger {
	public String LOGGER_FILENAME = "jester.log";
	public RealLogger() {
		super();
	}
	public void log(String message) {
		try {
			FileWriter logFile = new FileWriter(LOGGER_FILENAME, true);
			logFile.write(new Date() + " ");
			logFile.write(message);
			logFile.write("\r\n");
			logFile.close();
		} catch (IOException ioe) {
			throw new RuntimeException("Could not write to log file because of IOException " + ioe);
		}
	}
}
