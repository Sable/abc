/*
 * Created on 08-Feb-2005
 *
 */
package abcexer1.ast;

import polyglot.types.TypeSystem;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface SurroundAdviceDecl extends AdviceDecl {

	public AdviceSpec spec();
	public Pointcut pc();
	
	public AdviceDecl getBeforeAdviceDecl(AJNodeFactory nodeFactory, TypeSystem ts);
	public AdviceDecl getAfterAdviceDecl(AJNodeFactory nodeFactory, TypeSystem ts);
	
}
