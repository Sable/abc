package abc.bridge;

import java.util.ArrayList;
import java.util.List;

import abc.main.CompilerFailedException;

public class AbcMain {
	public static void compile(CompilationArgs cArgs) throws CompilationFailedException {
		SilentMain main=null;
		try {
			main = new SilentMain(cArgs.args);
			main.run();
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal arguments: "+e.getMessage());
			throw new CompilationFailedException(null);//System.exit(1);
		} catch (CompilerFailedException e) {
			throw new CompilationFailedException(main.getErrors());//System.exit(5);
		} finally {
			SilentMain.reset();
		}
	}
    public static class CompilationArgs {
    	String[] args;
    	public CompilationArgs(String[] args, String cp) {
			ArrayList currentArgs = new ArrayList();
			boolean cpFound = false;
			String currentCP = null;;
			for (int i=0; i<args.length; i++) {
				currentArgs.add(args[i]);
				if ("-cp".equals(args[i]) || "-classpath".equals(args[i])) {
					if (cp.length()==0)
						currentCP = args[++i];
					else
						currentCP = cp + ":" + args[++i];
					currentArgs.add(currentCP);

				}
				else if (args[i].startsWith("-")) {
					currentArgs.add(args[++i]);
				}
			}
			if (currentCP == null) {
				currentArgs.add(0, cp);
				currentArgs.add(0, "-cp");
				currentCP = cp;
			}
			currentArgs.toArray(this.args = new String[currentArgs.size()]);
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
