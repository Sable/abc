package abc.bridge;

import java.util.List;

import abc.main.CompilerFailedException;

public class AbcMain {
    public static void compile(CompilationArgs args) throws CompilationFailedException {
		SilentMain main=null;
		try {
			String[] mainArgs = new String[args.sources.length+2];
			mainArgs[0] = "-cp";
			mainArgs[1] = args.classpath;
			for (int i=0; i<args.sources.length; i++)
				mainArgs[i+2] = args.sources[i];
			main = new SilentMain(mainArgs);
			main.run();
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal arguments: "+e.getMessage());
			System.exit(1);
		} catch (CompilerFailedException e) {
			throw new CompilationFailedException(main.getErrors());//System.exit(5);
		}
    }
    public static class CompilationArgs {
    	String classpath;
    	String[] sources;
    	public CompilationArgs(String cp, String[] s) {
    		classpath = cp;
    		sources = s;
    	}
    }
    public static class CompilationFailedException extends Exception {
    	private List errors;
		CompilationFailedException(List e) {
			errors = e;
		}
		public List getErrors() {
			return errors;
		}
    }
}
