/*
 * Created on 08-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package abcexer1.ast;

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
	
	public AdviceDecl getBeforeAdviceDecl(Abcexer1NodeFactory nodeFactory);
	public AdviceDecl getAfterAdviceDecl(Abcexer1NodeFactory nodeFactory);
	
}
