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
import abc.aspectj.ast.AJNodeFactory_c;
import abc.aspectj.ast.AdviceFormal;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Before;
import abc.aspectj.ast.Before_c;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.Pointcut;
import abc.eaj.ast.GlobalPointcutDecl;
import abc.eaj.ast.GlobalPointcutDecl_c;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Abcexer1NodeFactory_c extends AJNodeFactory_c 
									implements Abcexer1NodeFactory
{
	public SurroundAdviceDecl SurroundAdviceDecl(Position pos, Flags flags, AdviceSpec spec,
					List throwTypes, Pointcut pc, Block body, Block afterBody) 
	{
		return new SurroundAdviceDecl_c(pos, flags, spec, throwTypes, pc, body, afterBody);
	}
	public Surround Surround(Position pos, List formals, TypeNode voidn) {
		Surround n = new Surround_c(pos,formals,voidn);
       //n = (Before)n.ext(extFactory.extBefore());
       //n = (Before)n.del(delFactory.delBefore());
		return n;
   }
}
