package jester;

import java.io.IOException;

public class RealCompiler implements Compiler {
	private Configuration myConfiguration;

	public RealCompiler(Configuration aConfiguration) {
		super();
		myConfiguration = aConfiguration;
	}

	public static void main(String args[]) throws IOException, SourceChangeException {
		new RealCompiler(new RealConfiguration(RealConfiguration.DEFAULT_CONFIGURATION_FILENAME)).compile(args[0]);
	}

	public void compile(String sourceFileName) throws SourceChangeException {
        
	    String[] commandLine = new String[4]; 
        commandLine[0] = myConfiguration.compilationCommand();
        commandLine[1] = "-classpath"; 
        commandLine[2] = System.getProperty(MainArguments.CLASSPATH_PROPERTY);
        commandLine[3] = sourceFileName;
		try {
			Util.runCommand(commandLine, myConfiguration.getLogger());
		} catch (IOException e) {
			throw new SourceChangeException("couldn't compile " + sourceFileName);
		}
	}
}