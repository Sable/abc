package abc.aspectj.ast;

import java.util.List;
import polyglot.types.*;
import polyglot.ast.MethodDecl;
import polyglot.ast.Local;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AspectJTypeSystem;

public interface PCIf extends Pointcut, MakesAspectMethods
{
	MethodDecl exprMethod(AspectJNodeFactory nf, AspectJTypeSystem ts, List formals, ParsedClassType container);
	PCIf liftMethod(AspectJNodeFactory nf);

    public void joinpointFormals(Local n);
}

