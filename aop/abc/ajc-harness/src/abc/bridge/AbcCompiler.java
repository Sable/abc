package abc.bridge;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.AjCompiler;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import polyglot.util.ErrorInfo;

public class AbcCompiler extends AjCompiler {
	final private String[] savedArgs;
	public AbcCompiler(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory,
		String[] args) {
		super(environment, policy, settings, requestor, problemFactory);
		savedArgs = args;
	}

	public AbcCompiler(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory,
		boolean parseLiteralExpressionsAsConstants,
		String[] args) {
		super(
			environment,
			policy,
			settings,
			requestor,
			problemFactory,
			parseLiteralExpressionsAsConstants);
		savedArgs = args;
	}

	public void compile(ICompilationUnit[] sourceUnits) {
		ICompilationUnit unit = null;
		String classpath = System.getProperty("java.class.path");
		totalUnits = sourceUnits.length;
		CompilationResult[] compilationResults = new CompilationResult[totalUnits];
		Map compilationResultsMap = new HashMap(compilationResults.length); // mapping a source unit name to its corresponding CompilationResult object
		Set compilationResultsWithErrors = new HashSet();
		Map referenceContexts = new HashMap(compilationResults.length); // mapping a compilation result to a reference context object
		String[] sourceUnitNames = new String[totalUnits];
		for (int i=0; i < totalUnits; i++) {
			compilationResults[i] = new CompilationResult(sourceUnits[i], 1, 1, 100);
			sourceUnitNames[i] = new String(sourceUnits[i].getFileName());
			compilationResultsMap.put(sourceUnitNames[i], compilationResults[i]);
		}
		//System.out.println("calling abc on files:" + sourceUnitNames.toString());
		AbcMain.CompilationArgs abcArgs = new AbcMain.CompilationArgs(savedArgs, classpath);
		try {
			AbcMain.compile(abcArgs);
			//System.out.println("compilation of " + sourceUnitNames.toString() + " completed successfully");
		} catch (AbcMain.CompilationFailedException e) {
//				e.printStackTrace();
			//System.out.println("compilation of " + sourceUnitNames.toString() + " failed");
			if (e.getErrors() != null)
				for (Iterator it= e.getErrors().iterator(); it.hasNext();) {
					ErrorInfo ei= (ErrorInfo) it.next();
					//this.problemReporter.problemFactory.createProblem(originatingFilaName, problemId, problemArguments, messageArguments, severity, startPos, endPos, lineNumber);
					if (ei.getPosition() == null) continue; //TODO: see how to report a problem with no location
					IProblem error= problemReporter.problemFactory.createProblem(
						ei.getPosition().file().toCharArray(), 
						0, 
						new String[] { ei.getErrorString() },
						new String[] { ei.getMessage() }, 
						problemId(ei.getErrorKind()), 
						ei.getPosition().column(), 
						ei.getPosition().column()+1, 
						ei.getPosition().line());
					CompilationResult compilationResult= ((CompilationResult) compilationResultsMap.get(ei.getPosition().file()));
					ReferenceContext referenceContext= (ReferenceContext) referenceContexts.get(compilationResult);
					if (referenceContext == null) {
						referenceContext = new TypeDeclaration(compilationResult);
						referenceContexts.put(compilationResult, referenceContext);
					}
					compilationResultsWithErrors.add(compilationResult);
					compilationResult.record(error, referenceContext);//problemReporter.referenceContext);	
				}
/*		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException();
*/		}
		for (Iterator it= compilationResultsWithErrors.iterator(); it.hasNext();) {
			CompilationResult compilationResult= (CompilationResult) it.next();
			//try { 
			requestor.acceptResult(compilationResult.tagAsAccepted()); 
			//} catch (NullPointerException e) {
				// TODO: seems like a bug in the harness that somtimes throws a null pointer exception (probably because referenceContext was null at the time of calling .record())
			//}
		}
	}
	
	private int problemId(int abcId) {
		switch (abcId) {
			case ErrorInfo.INTERNAL_ERROR:
			case ErrorInfo.IO_ERROR: 
			case ErrorInfo.POST_COMPILER_ERROR:
				//return ProblemSeverities.Ignore;
			case ErrorInfo.LEXICAL_ERROR:
			case ErrorInfo.SEMANTIC_ERROR:
			case ErrorInfo.SYNTAX_ERROR:
				return ProblemSeverities.Error;
			case ErrorInfo.WARNING:
			default:
				return ProblemSeverities.Warning;
		}
	}

	static void compile(PrintStream out) {
/*		out.println("Simple2.java:1: Syntax error.");
		out.println("public aspect2 Simple2 percflow(adviceexecution()) {");
		out.println("       ^");
		out.println("");
		out.println("aspectjc: Couldn't repair and continue parse");
		out.println("aspectjc: Unable to recover from previous errors.");
		out.println("aspectjc: Couldn't repair and continue parse");
		out.println("Dumping AST for Simple2.java (before parse(Simple2.java)) after parse");
		out.println("4 errors.");*/
	}
}
