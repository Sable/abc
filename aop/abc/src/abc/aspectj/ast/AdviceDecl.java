package abc.aspectj.ast;

import polyglot.ast.MethodDecl;
import abc.aspectj.types.AspectJTypeSystem;


public interface AdviceDecl extends MethodDecl
{
    /* new stuff to be added */
   
   	MethodDecl proceedDecl(AspectJNodeFactory nf,AspectJTypeSystem ts);
   	MethodDecl methodDecl(AspectJNodeFactory nf,AspectJTypeSystem ts);
   	
}
