package jester;

import java.io.*;
import java.util.StringTokenizer;

public class RealMutationsList implements MutationsList {
	public static final String DEFAULT_MUTATIONS_FILENAME = "mutations.cfg";
	
	private String myFileName;
	private PrintStream myErrorStream;
	
	public RealMutationsList(String fileName, PrintStream errorStream) {
		super();
		myFileName = fileName;
		myErrorStream = errorStream;
	}
	public RealMutationsList(String fileName) {
		this(fileName, System.err);
	}
	public void visit(BufferedReader aBufferedReader, MutationMaker aMutationMaker) throws IOException, SourceChangeException {
		String line = aBufferedReader.readLine();
		while (line != null) {
			visitLine(line, aMutationMaker);
			line = aBufferedReader.readLine();
		}
	}
	public void visit(MutationMaker aMutationMaker) throws SourceChangeException {
		try {
			InputStream mutationsFile = ClassLoader.getSystemResourceAsStream(myFileName);
			if (mutationsFile == null) {
				myErrorStream.println("Warning - could not find "+DEFAULT_MUTATIONS_FILENAME+" so using default mutations.");
				aMutationMaker.mutate("true", "false");
				aMutationMaker.mutate("false", "true");
				aMutationMaker.mutate("if(", "if(true ||");
				aMutationMaker.mutate("if (", "if (true ||");
				aMutationMaker.mutate("if(", "if(false &&");
				aMutationMaker.mutate("if (", "if (false &&");
				aMutationMaker.mutate("==", "!=");
				aMutationMaker.mutate("!=", "==");
			} else {
				BufferedReader aBufferedReader = new BufferedReader(new InputStreamReader(mutationsFile));

				visit(aBufferedReader, aMutationMaker);
			}
		} catch (IOException ex) {
			throw new SourceChangeException("could not visit mutations " + ex);
		}
	}
	private void visitLine(String line, MutationMaker aMutationMaker) throws SourceChangeException {
		if(line.length() == 0){
			return;
		}
		String delimiter = line.substring(0, 1);
		StringTokenizer aStringTokenizer = new StringTokenizer(line.substring(1), delimiter, false);
		int numberOfItems = 2;
		String[] strings = new String[numberOfItems];
		int i = 0;
		while (i < numberOfItems && aStringTokenizer.hasMoreTokens()) {
			String s = aStringTokenizer.nextToken();
			strings[i] = s;
			i++;
		}
		if (i != numberOfItems) {
			return;
		}
		aMutationMaker.mutate(strings[0], strings[1]);
	}
}