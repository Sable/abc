/*
 * Created on 08-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package abcexer1.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AdviceFormal;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Abcexer1NodeFactory extends AJNodeFactory {
	public SurroundAdviceDecl SurroundAdviceDecl(Position pos, Flags flags, AdviceSpec spec,
			List throwTypes, Pointcut pc, Block body, Block afterBody);
	public Surround Surround(Position pos, List formals, TypeNode voidn);
}
