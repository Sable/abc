
package abc.aspectj.ast;

import polyglot.ast.Call;
import polyglot.ast.MethodDecl;

import abc.aspectj.ast.MakesAspectMethods;

public interface ProceedCall extends Call, MakesAspectMethods {

	ProceedCall proceedMethod(MethodDecl md,AdviceDecl ad);
	
}
