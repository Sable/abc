package abc.bridge;

import java.util.List;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.util.SilentErrorQueue;

public class SilentMain extends abc.main.Main {
	private SilentErrorQueue errorQueue;
	public SilentMain(String[] args) throws IllegalArgumentException {
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
