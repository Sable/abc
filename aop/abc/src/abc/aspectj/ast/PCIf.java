package abc.aspectj.ast;

import java.util.List;
import polyglot.types.*;
import polyglot.ast.MethodDecl;
import polyglot.ast.Local;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.visit.TransformsAspectReflection;
import abc.aspectj.types.AJTypeSystem;

public interface PCIf extends Pointcut, MakesAspectMethods, TransformsAspectReflection
{
	MethodDecl exprMethod(AspectJNodeFactory nf, AJTypeSystem ts, List formals, ParsedClassType container);
	PCIf liftMethod(AspectJNodeFactory nf);

    public void joinpointFormals(Local n);
}

