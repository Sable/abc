package abc.aspectj.ast;

import java.util.List;
import polyglot.types.*;
import polyglot.ast.MethodDecl;
import abc.aspectj.types.AspectJTypeSystem;

public interface PCIf extends Pointcut
{
	MethodDecl exprMethod(AspectJNodeFactory nf, AspectJTypeSystem ts, List formals, ParsedClassType container);
	PCIf liftMethod(AspectJNodeFactory nf);
}
