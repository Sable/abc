
package abc.aspectj.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.frontend.Job;
import polyglot.visit.ContextVisitor;
import polyglot.types.SemanticException;

import abc.aspectj.ast.DeclareParentsExt;
import abc.aspectj.ast.DeclareParentsImpl;

public class DeclareParentsAmbiguityRemover extends ContextVisitor {

    public DeclareParentsAmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    public Node leaveCall(Node n) throws SemanticException {
	if (n instanceof DeclareParentsExt) {
	    System.err.print("pip");
	    return ((DeclareParentsExt)n).disambiguate(this);
	}
	if (n instanceof DeclareParentsImpl) {
	    System.err.print("pop");
	    return ((DeclareParentsImpl)n).disambiguate(this);
	}
	return n;
    }
}
