package abc.bridge;

import java.io.File;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.AjCompiler;
import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.problem.AjProblemReporter;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.bridge.IMessageHandler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class AbcBuildManager extends AjBuildManager {
	final private String[] savedArgs;
	public AbcBuildManager(IMessageHandler holder, String[] args) {
		super(holder);
		savedArgs= args;
	}

	public void performCompilation(List files) {
/*		if (progressListener != null) {
			compiledCount = 0;
			sourceFileCount = files.size();
			progressListener.setText("compiling source files");
		}*/
		//System.err.println("got files: " + files);
		String[] filenames = new String[files.size()];
		String[] encodings = new String[files.size()];
		//System.err.println("filename: " + this.filenames);
		for (int i=0; i < files.size(); i++) {
			filenames[i] = ((File)files.get(i)).getPath();
		}
		
		List cps = buildConfig.getFullClasspath();
		String[] classpaths = new String[cps.size()];
		for (int i=0; i < cps.size(); i++) {
			classpaths[i] = (String)cps.get(i);
		}
		
		//System.out.println("compiling");
		INameEnvironment environment = getLibraryAccess(classpaths, filenames);
		
/*		if (!state.classesFromName.isEmpty()) {
			environment = new StatefulNameEnvironment(environment, state.classesFromName);
		}*/
		
		AjCompiler compiler = new AbcCompiler(
			environment,
			DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			buildConfig.getJavaOptions(),
			getBatchRequestor(),
			getProblemFactory(),
			savedArgs);
			
			
		AjProblemReporter pr =
			new AjProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			compiler.options, getProblemFactory());
		
		compiler.problemReporter = pr;
			
		AjLookupEnvironment le =
			new AjLookupEnvironment(compiler, compiler.options, pr, environment);
		EclipseFactory factory = new EclipseFactory(le);
//		ew.setLint(bcelWorld.getLint());
//		ew.setXnoInline(buildConfig.isXnoInline());
		le.factory = factory;
		pr.factory = factory;
		le.factory.buildManager = this;
		
		compiler.lookupEnvironment = le;
		
		compiler.parser =
			new Parser(
				pr, 
				compiler.options.parseLiteralExpressionsAsConstants);

		CompilerOptions options = compiler.options;

		options.produceReferenceInfo(true); //TODO turn off when not needed
		
		compiler.compile(getCompilationUnits(filenames, encodings));
		
		// cleanup
		environment.cleanup();
	}

}
