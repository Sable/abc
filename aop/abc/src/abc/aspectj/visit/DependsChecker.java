/*
 * check dependencies between named pointcuts, and also abstract flags
 */
 
package abc.aspectj.visit;

import polyglot.ast.NodeFactory;
import polyglot.ast.Node;

import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.visit.ContextVisitor;

/**
 * check dependencies between named pointcuts, and also abstract flags
 * @author Oege de Moor
 */
public class DependsChecker extends ContextVisitor {

	
	public DependsChecker(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}
	
	protected Node leaveCall(Node n) throws SemanticException {
		if (n instanceof DependsCheck) {
			return (((DependsCheck)n).checkDepends(this));
		}
		return n;
	}


}
