/*
 * Created on 16-Aug-2004
 */
package abc.aspectj.visit;

import polyglot.ast.NodeFactory;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.visit.NodeVisitor;
import polyglot.visit.ContextVisitor;

import abc.aspectj.types.AJContext;

/**
 * @author Oege de Moor
 */
public class AnonBodyITDs extends ContextVisitor {


	public AnonBodyITDs(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
		// TODO Auto-generated constructor stub
	}

	protected NodeVisitor enterCall(Node n) throws SemanticException {
		// we only inspect the topmost node
		return this.bypassChildren(n);
	}
	   
	protected Node leaveCall(Node n) throws SemanticException {
		// if that node is the body of an anonymous class, we 
		// add interface itds as needed
		AJContext ajc = (AJContext) context();
		ClassType anonType = ajc.currentClass();
	    if (anonType !=null && anonType.isAnonymous()) {
	   		 InterfaceITDs.process(anonType);
	   }
	   return n;
	   }
}
