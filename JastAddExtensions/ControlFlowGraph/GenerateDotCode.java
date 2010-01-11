
import AST.*;
import java.util.*;
import java.io.*;

public class GenerateDotCode extends Frontend {

	public static void main(String[] args) {

		if (args.length == 0) {
			System.err.println("error: You need to supply a filename and then JastAddJ arguments");
			System.exit(1);
		}

		try {
		// Setup files
		String filePath = args[0];
		setupFile(filePath);

		// Move arguments
		String[] argList = new String[args.length-1];
		for (int i = 0; i < argList.length; i++) {
			argList[i] = args[i+1];
		}

		writeHeader();
		compile(argList);
		writeFooter();

		closeFile();

		} catch (Exception e) {
			System.err.println("error: " + e.getMessage());
			System.exit(1);
		}
	}

	protected void processNoErrors(CompilationUnit unit) {
		out.println("\t//" + unit.relativeName());
		unit.visualiseCallGraphUsingDot(out);
	}

	public static boolean compile(String args[]) {
		return new GenerateDotCode().process(
    	    args,
    	    new BytecodeParser(),
    	    new JavaParser() {
    	      parser.JavaParser parser = new parser.JavaParser();
    		      public CompilationUnit parse(java.io.InputStream is, String fileName) 
					throws java.io.IOException, beaver.Parser.Exception {
		            return parser.parse(is, fileName);
	          }
			
		});
	}

  	private static PrintStream out;

  	private static void setupFile(String filePath) throws IOException {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		} 
		file.createNewFile();
		out = new PrintStream(file);
	}

	private static void writeHeader() {
		out.println("digraph G  {");
        out.println("\tsize=\"300,150\"");
        out.println("\tcenter=\"\"");
        out.println("\tranksep=2;");
        out.println("\tratio=auto;");
	}

	private static void writeFooter() {
		out.println("}");
	}

  	private static void closeFile() throws IOException {
		out.close();
  	}
}

