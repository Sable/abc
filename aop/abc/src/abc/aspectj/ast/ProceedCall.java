
package abc.aspectj.ast;

import polyglot.ast.Call;
import polyglot.ast.MethodDecl;

import abc.aspectj.ast.MakesAspectMethods;

/**
 * A reference to "proceed(x1,x2)" inside a piece of around advice.
 * @author Oege de Moor
 *
 */
public interface ProceedCall extends Call, MakesAspectMethods {

	ProceedCall proceedMethod(MethodDecl md);
	
}
