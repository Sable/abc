
import AST.*;
import java.util.*;
import java.io.*;

public class GenerateDotCode extends Frontend {

	public static void main(String[] args) {

		if (args.length == 0) {
			System.err.println("error: You need to supply a filename base " + 
				"and then JastAddJ arguments");
			System.exit(1);
		}

		try {
		// Setup files
		String filePathBase = args[0];
		setupFiles(filePathBase);

		// Move arguments
		String[] argList = new String[args.length-1];
		for (int i = 0; i < argList.length; i++) {
			argList[i] = args[i+1];
		}

		writeDotHeader();
		compile(argList);
		writeDotFooter();

		closeFiles();

		} catch (Exception e) {
			System.err.println("error: " + e.getMessage());
			System.exit(1);
		}
	}

	protected void processNoErrors(CompilationUnit unit) {
		unit.visualiseCallGraphUsingDot(dotOut, edgeBuffer);
		unit.visualiseCallGraphUsingText(textOut);
		unit.visualiseIntraTypeFlowText(textOut);
	}

	public static boolean compile(String args[]) {
		return new GenerateDotCode().process(
    	    args,
    	    new BytecodeParser(),
    	    new JavaParser() {
    	      parser.JavaParser parser = new parser.JavaParser();
    		      public CompilationUnit parse(java.io.InputStream is, 
					String fileName) throws java.io.IOException,
					beaver.Parser.Exception {
		            return parser.parse(is, fileName);
	          }
			
		});
	}

	private static StringBuffer edgeBuffer;
  	private static PrintStream dotOut;
  	private static PrintStream textOut;

  	private static void setupFiles(String filePathBase) throws IOException {
		// Create dot file
		File dotFile = new File(filePathBase+".dot");
		if (dotFile.exists()) {
			dotFile.delete();
		} 
		dotFile.createNewFile();
		dotOut = new PrintStream(dotFile);
		// Create simple text file
		File textFile = new File(filePathBase+".text");
		if (textFile.exists()) {
			textFile.delete();
		}
		textFile.createNewFile();
		textOut = new PrintStream(textFile);
	}

	private static void writeDotHeader() {
		dotOut.println("digraph G  {");
        dotOut.println("\tsize=\"300,150\"");
        dotOut.println("\tcenter=\"\"");
        dotOut.println("\tratio=0.02;");
        dotOut.println("\tratio=auto;");
		dotOut.println("\tnode[width=0.2,height=0.2,fontsize=25]");
		edgeBuffer = new StringBuffer();
	}

	private static void writeDotFooter() {
		dotOut.println(edgeBuffer.toString());
		dotOut.println("}");
	}

  	private static void closeFiles() throws IOException {
		dotOut.close();
		textOut.close();
  	}
}

