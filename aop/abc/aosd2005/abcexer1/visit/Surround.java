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
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AspectBody;
import abcexer1.ast.Abcexer1NodeFactory;
import abcexer1.ast.SurroundAdviceDecl;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Surround extends NodeVisitor {

	Abcexer1NodeFactory nodeFactory;
	
	public Surround(Abcexer1NodeFactory nodeFactory) {
		this.nodeFactory=nodeFactory;
	}
	Stack afterAdvices=new Stack();
	
	boolean insideSurroundAdviceDecl=false;
	
	public NodeVisitor enter(Node n) {		
		if (n instanceof AspectBody) {
			afterAdvices.push(new LinkedList());
		}		
        return this;
    }
	public Node leave(Node old, Node n, NodeVisitor v) {		
		if (n instanceof AspectBody) { // leaving the aspect?
			// add all generated pieces of after advice
			AspectBody oldBody=(AspectBody)n;
			List members=oldBody.members();
			
			List advices=(List)afterAdvices.peek();
			for (Iterator it=advices.iterator();it.hasNext();) {
				SurroundAdviceDecl decl=(SurroundAdviceDecl)it.next();
				members.add(decl.getAfterAdviceDecl(nodeFactory));
			}
			AspectBody aspectBody=nodeFactory.AspectBody(oldBody.position(), members);
			
			afterAdvices.pop();
			return aspectBody;
		} else if (n instanceof SurroundAdviceDecl) {
			// Turn any surround advice decl into a piece of before advice.
			// Also store a reference 
			SurroundAdviceDecl decl=(SurroundAdviceDecl)n;
			List advices=(List)afterAdvices.peek();
			advices.add(decl);
			return decl.getBeforeAdviceDecl(nodeFactory);
		}
        return n;
    }
}
