/*
 * Created on 08-Feb-2005
 */
package abcexer1.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceDecl_c;
import abc.aspectj.ast.AdviceFormal;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;

/**
 * @author Sascha Kuzins
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
	
	public AdviceSpec spec() { return (AdviceSpec)spec;}
	public Pointcut pc() { return (Pointcut)pc;}
	public Block afterBody() { return (Block) afterBody;}
	
	public AdviceDecl getBeforeAdviceDecl(AJNodeFactory nf, TypeSystem ts) {
		Surround spec=(Surround)this.spec;
		
		SurroundAdviceDecl_c result=(SurroundAdviceDecl_c)this.copy();
		result.spec=spec.getBeforeSpec(nf);
		return result;
	}
	public AdviceDecl getAfterAdviceDecl(AJNodeFactory nf, TypeSystem ts){
		Surround spec=(Surround)this.spec;
		
		AdviceDecl result=nf.AdviceDecl(position(), flags(), 
				spec.getAfterSpec(nf),
				throwTypes(), pc(), 
				afterBody() // <= set after body for after advice
				);

		// set a new MethodInstance 
		result=(AdviceDecl)result.methodInstance(ts.methodInstance(
				this.methodInstance().position(), 
				this.methodInstance().container(), 
				this.methodInstance().flags(),  
				this.methodInstance().returnType(), "surroundAfter", 
				this.methodInstance().formalTypes(), 
				this.methodInstance().throwTypes() ));
		
		return result;
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
		TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
		List formals = visitList(this.formals, v);
		List throwTypes = visitList(this.throwTypes, v);
		AdviceFormal retval = (AdviceFormal) visitChild(this.retval,v);
		Pointcut pc = (Pointcut) visitChild(this.pc,v);
		Block body = (Block) visitChild(this.body, v);
		Block afterBody = (Block) visitChild(
				this.afterBody, v);
		return reconstruct(returnType, formals, throwTypes, body, spec, retval, pc, afterBody);
	}

}
