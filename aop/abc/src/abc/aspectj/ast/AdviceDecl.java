package abc.aspectj.ast;

import polyglot.ast.MethodDecl;
import polyglot.ast.Local;
import polyglot.types.CodeInstance;

import abc.aspectj.types.AspectJTypeSystem;


public interface AdviceDecl extends MethodDecl, MakesAspectMethods
{
    /* new stuff to be added */
   
   	MethodDecl proceedDecl(AspectJNodeFactory nf,AspectJTypeSystem ts);
   	MethodDecl methodDecl(AspectJNodeFactory nf,AspectJTypeSystem ts);
   	void joinpointFormals(Local n);
   	boolean hasJoinPoint();
   	boolean hasJoinPointStaticPart();
   	void proceedContainer(CodeInstance ci);
}
