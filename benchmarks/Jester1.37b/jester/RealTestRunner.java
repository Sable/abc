package jester;

import java.io.IOException;
import java.util.Vector;

public class RealTestRunner implements TestRunner {
	private Configuration myConfiguration;
	private String myTestClassName;

	public static void main(String args[]) throws IOException, SourceChangeException {
		System.out.println(new RealTestRunner(new RealConfiguration(RealConfiguration.DEFAULT_CONFIGURATION_FILENAME), args[0]).testsRunWithoutFailures());
	}

	public boolean testsRunWithoutFailures() throws SourceChangeException {
        
        String[] runCmd = new String[5];
        runCmd[0] = "java";
        runCmd[1] = "-cp";
        runCmd[2] = System.getProperty(MainArguments.CLASSPATH_PROPERTY);
        runCmd[3] = TestRunnerImpl.class.getName();
        runCmd[4] = myTestClassName;
		try {
			Vector output = Util.runCommand(runCmd, myConfiguration.getLogger());
			
			return output.size() > 0 && output.lastElement().equals(myConfiguration.testsPassString());
		} catch (IOException e) {
			throw new SourceChangeException("couldn't run tests " + e.getMessage());
		}
	}

	public RealTestRunner(Configuration aConfiguration, String testClassName) {
		super();
		myTestClassName = testClassName;
		myConfiguration = aConfiguration;
	}
}