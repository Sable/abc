package abc.testing;

import java.util.List;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.util.SilentErrorQueue;
import abc.main.CompilerAbortedException;

public class SilentMain extends abc.main.Main {
	private SilentErrorQueue errorQueue;
	public SilentMain(String[] args) throws IllegalArgumentException, CompilerAbortedException {
		super(args);
	}
	
	protected Compiler createCompiler(ExtensionInfo ext) {
		errorQueue = new SilentErrorQueue(ext.getOptions().error_count,
			ext.compilerName());
		return new Compiler(ext, errorQueue);
	}
	List getErrors() {
		return (errorQueue != null) ? errorQueue.getErrors() : null;
	}
}
