package abc.bridge;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.AjCompiler;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import polyglot.util.ErrorInfo;

public class AbcCompiler extends AjCompiler {

	public AbcCompiler(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory) {
		super(environment, policy, settings, requestor, problemFactory);
	}

	public AbcCompiler(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory,
		boolean parseLiteralExpressionsAsConstants) {
		super(
			environment,
			policy,
			settings,
			requestor,
			problemFactory,
			parseLiteralExpressionsAsConstants);
	}

	public void compile(ICompilationUnit[] sourceUnits) {
		ICompilationUnit unit = null;
		String classpath = System.getProperty("java.class.path");
		int i = 0;
		totalUnits= sourceUnits.length;
		for (; i < totalUnits; i++) {
			unit = sourceUnits[i];
			String[] args= new String[1];
			args[0]= new String(unit.getFileName());
			System.out.println("calling abc on file:" + args[0]);
			CompilationResult compilationResult= new CompilationResult(unit, 1, 1, 1);
			AbcMain.CompilationArgs abcArgs = new AbcMain.CompilationArgs(classpath, args);
			try {
				AbcMain.compile(abcArgs);
			} catch (AbcMain.CompilationFailedException e) {
//				e.printStackTrace();
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
					compilationResult.record(error, problemReporter.referenceContext);	
				}
/*			} catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException();
*/			}
			requestor.acceptResult(compilationResult.tagAsAccepted());
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
