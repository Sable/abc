
package abc.aspectj.ast;

import polyglot.ast.Call;
import polyglot.ast.MethodDecl;

public interface ProceedCall extends Call {

	ProceedCall proceedMethod(MethodDecl md);
	
}
