package arc.aspectj.ast;

import java.util.List;
import polyglot.ast.MethodDecl;
import arc.aspectj.types.AspectJTypeSystem;

public interface PCIf extends Pointcut
{
	MethodDecl exprMethod(AspectJNodeFactory nf, AspectJTypeSystem ts, List formals);
	PCIf liftMethod(AspectJNodeFactory nf);
}
