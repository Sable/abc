package abc.aspectj.ast;

import polyglot.ast.MethodDecl;
import polyglot.ast.Local;
import polyglot.types.CodeInstance;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.TransformsAspectReflection;

/** @author Oege de Moor */

public interface AdviceDecl extends MethodDecl, 
                                    MakesAspectMethods, 
                                    TransformsAspectReflection
{

    /** generate a dummy MethodDecl for the proceed. Only applies to around advice. */
   	MethodDecl proceedDecl(AJNodeFactory nf,AJTypeSystem ts);
   	
   	/** generate a MethodDecl for the advice body */
   	MethodDecl methodDecl(AJNodeFactory nf,AJTypeSystem ts);
   	
   	/** register the use of "thisJoinPoint" etc.
   	 * @param n  test whether this local is "thisJoinPoint" etc();
   	 */
   	void joinpointFormals(Local n);
   	
   	/** does "thisJoinPoint" occur in the advice body? */
   	boolean hasJoinPoint();
   	
   	/** does "thisJoinPointStaticPart" occur in the advice body? */
   	boolean hasJoinPointStaticPart();
   	
   	/** register methods or constructors that are local to the advice, for later use in weaver
   	 * @param ci   code instance to register
   	 */
   	void localMethod(CodeInstance ci);
}
