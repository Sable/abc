/*
 * Created on 08-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package abcexer1.ast;

import java.util.HashSet;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import polyglot.util.UniqueID;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceDecl_c;
import abc.aspectj.ast.AdviceFormal;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.Pointcut;
import abc.eaj.ast.GlobalPointcutDecl_c;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SurroundAdviceDecl_c extends AdviceDecl_c 
									implements SurroundAdviceDecl
{
    protected Block afterBody;
    
	public SurroundAdviceDecl_c(Position pos, Flags flags, AdviceSpec spec,
			List throwTypes, Pointcut pc, Block body, Block afterBody) {		
		super(pos, flags, spec, throwTypes, pc, body);
		this.afterBody=afterBody;
	}
	
	public AdviceSpec spec() { return (AdviceSpec)spec.copy(); }
	public Pointcut pc() { return (Pointcut)pc; }
	public Block afterBody() { return (Block) afterBody.copy(); }
	
	public AdviceDecl getBeforeAdviceDecl(Abcexer1NodeFactory nodeFactory) {
		Surround spec=(Surround)this.spec;
		return nodeFactory.AdviceDecl(position(), flags(), 
				spec.getBeforeSpec(nodeFactory),
				throwTypes(), pc(), body());
	}
	public AdviceDecl getAfterAdviceDecl(Abcexer1NodeFactory nodeFactory){
		Surround spec=(Surround)this.spec;
		return nodeFactory.AdviceDecl(position(), flags(), 
				spec.getAfterSpec(nodeFactory),
				throwTypes(), pc(), afterBody());
	}
	
	
	protected SurroundAdviceDecl_c reconstruct(
	  			TypeNode returnType, 
		       List formals, 
		       List throwTypes,
		       Block body,
		       AdviceSpec spec,
		       AdviceFormal retval,
		       Pointcut pc,
			   Block afterBody) {
		if (afterBody != this.afterBody) {
			SurroundAdviceDecl_c n = (SurroundAdviceDecl_c) copy();
			n.afterBody=afterBody;
			return (SurroundAdviceDecl_c) n.reconstruct(returnType, formals, throwTypes, body, spec, retval, pc);
		}
		return (SurroundAdviceDecl_c) super.reconstruct(returnType, formals, throwTypes, body, spec, retval, pc);
	}

	public Node visitChildren(NodeVisitor v) {
		Node n = super.visitChildren(v);

		Block afterBody = (Block) visitChild(
				this.afterBody, v);

		return n; //reconstruct(n., afterBody, pc);
	}

}
