package abc.aspectj.ast;

import polyglot.ast.MethodDecl;
import polyglot.ast.Local;
import polyglot.types.CodeInstance;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.TransformsAspectReflection;

public interface AdviceDecl extends MethodDecl, 
                                    MakesAspectMethods, 
                                    TransformsAspectReflection
{
    /* new stuff to be added */
   
   	MethodDecl proceedDecl(AspectJNodeFactory nf,AJTypeSystem ts);
   	MethodDecl methodDecl(AspectJNodeFactory nf,AJTypeSystem ts);
   	void joinpointFormals(Local n);
   	boolean hasJoinPoint();
   	boolean hasJoinPointStaticPart();
   	void localMethod(CodeInstance ci);
}
