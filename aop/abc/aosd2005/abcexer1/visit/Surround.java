/*
 * Created on 08-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package abcexer1.visit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AspectBody;
import abcexer1.ast.SurroundAdviceDecl;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Surround extends ContextVisitor {

	AJNodeFactory nodeFactory;
	
	/**
	 * @param job
	 * @param ts
	 * @param nf
	 */
	public Surround(Job job, TypeSystem ts, AJNodeFactory nf) {
		super(job, ts, nf);
		this.nodeFactory=nf;
		// TODO Auto-generated constructor stub
	}
	//public Surround(Abcexer1NodeFactory nodeFactory) {
	//	this.nodeFactory=nodeFactory;
	//}
	
	Stack afterAdvices=new Stack();
	
	
	public NodeVisitor enter(Node parent, Node n) {		
		if (n instanceof AspectBody) {
			afterAdvices.push(new LinkedList());
		}		
        return super.enter(n);
    }
	public Node leave(Node parent,  Node old, Node n, NodeVisitor v) {
		n = super.leave(old, n, v);
		if (n instanceof AspectBody) { // leaving the aspect?
			// add all generated pieces of after advice
			AspectBody oldBody=(AspectBody)n;
			List members=new LinkedList(oldBody.members());
			
			for (Iterator it=members.iterator(); it.hasNext();) {
				Node node=(Node)it.next();
				//System.out.println("member: " +  node + " class: " + node.getClass());
			}
				
			
			List advices=(List)afterAdvices.peek();
			for (Iterator it=advices.iterator();it.hasNext();) {
				SurroundAdviceDecl decl=(SurroundAdviceDecl)it.next();
				members.add(decl.getAfterAdviceDecl(nodeFactory, ts)); ///
			}
			afterAdvices.pop();
			
			AspectBody aspectBody=nodeFactory.AspectBody(oldBody.position(), members);			
			return aspectBody;
		} else if (n instanceof SurroundAdviceDecl) {
			// Turn any surround advice decl into a piece of before advice.
			// Also store a reference 
			SurroundAdviceDecl decl=(SurroundAdviceDecl)n;
			List advices=(List)afterAdvices.peek();
			advices.add(decl);
			return decl.getBeforeAdviceDecl(nodeFactory, ts);
		}
        return n;
    }
}
