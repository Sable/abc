/*
 * Created on 08-Feb-2005
 *
 */
package abcexer1.visit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AspectBody;
import abcexer1.ast.SurroundAdviceDecl;

/**
 * @author Sascha Kuzins
 * 
 * This pass turns any piece of surround advice into a piece of before advice
 * and stores a copy of the advice in a list.
 * When leaving the aspect, it generates a piece of after advice for 
 * each advice declaration from the list.
 * 
 */
public class Surround extends ContextVisitor {

	AJNodeFactory nodeFactory;
	
	public Surround(Job job, TypeSystem ts, AJNodeFactory nf) {
		super(job, ts, nf);
		this.nodeFactory=nf;
	}
	
	// Stack of advice lists to deal with nested aspects
	Stack afterAdviceListsStack=new Stack();
	List getAfterAdviceList() { 
		return (List)afterAdviceListsStack.peek(); 
	}
	
	public NodeVisitor enter(Node parent, Node n) {		
		if (n instanceof AspectBody) {
			afterAdviceListsStack.push(new LinkedList());
		}		
        return super.enter(n);
    }
	public Node leave(Node parent,  Node old, Node n, NodeVisitor v) {
		n = super.leave(old, n, v);
		if (n instanceof SurroundAdviceDecl) {
			// Turn any surround advice decl into a piece of before advice.
			// Also store a reference for later after advice generation
			SurroundAdviceDecl decl=(SurroundAdviceDecl)n;
			getAfterAdviceList().add(0, decl); // reverse order			
			return decl.getBeforeAdviceDecl(nodeFactory, ts);			
		} else if (n instanceof AspectBody) { // leaving the aspect?
			// generate and add all pieces of after advice
			AspectBody oldBody=(AspectBody)n;
			List members=new LinkedList(oldBody.members());
			
			int line=Integer.MAX_VALUE / 2; 
			for (Iterator it=getAfterAdviceList().iterator();it.hasNext();) {
				SurroundAdviceDecl decl=(SurroundAdviceDecl)it.next();
				
				AdviceDecl newDecl=decl.getAfterAdviceDecl(nodeFactory, ts);				

				// Invent a position for this advice declaration for precedence
				newDecl=(AdviceDecl)newDecl.position(new Position(decl.position().file(), 
						line++, 
						0
						)); 
				
				members.add(newDecl);
			}
			afterAdviceListsStack.pop();
			
			AspectBody aspectBody=nodeFactory.AspectBody(oldBody.position(), members);			
			return aspectBody;
		} 
		
        return n;
    }
}
