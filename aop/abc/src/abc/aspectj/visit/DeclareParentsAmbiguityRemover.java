
package abc.aspectj.visit;

import polyglot.ast.Node;
import polyglot.ast.ClassMember;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.frontend.Job;
import polyglot.visit.NodeVisitor;
import polyglot.visit.ContextVisitor;
import polyglot.types.SemanticException;

import abc.aspectj.ast.DeclareParentsExt;
import abc.aspectj.ast.DeclareParentsImpl;

public class DeclareParentsAmbiguityRemover extends ContextVisitor {

    public DeclareParentsAmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected NodeVisitor enterCall(Node n) throws SemanticException {
	if (n instanceof ClassMember && !(n instanceof DeclareParentsExt || n instanceof DeclareParentsImpl)) {
	    return bypass(n);
	}
	return this;
    }

    protected Node leaveCall(Node n) throws SemanticException {
	if (n instanceof DeclareParentsExt) {
	    return ((DeclareParentsExt)n).disambiguate(this);
	}
	if (n instanceof DeclareParentsImpl) {
	    return ((DeclareParentsImpl)n).disambiguate(this);
	}
	return n;
    }
}
