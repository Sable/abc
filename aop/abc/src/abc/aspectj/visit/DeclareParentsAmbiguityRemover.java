
package abc.aspectj.visit;


import polyglot.ast.Node;
import polyglot.ast.Ambiguous;
import polyglot.ast.QualifierNode;
import polyglot.ast.AmbTypeNode;
import polyglot.ast.AmbQualifierNode;
import polyglot.ast.ClassMember;
import polyglot.ast.NodeFactory;
import polyglot.ast.ClassDecl;

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
	if (n instanceof ClassMember && !(n instanceof ClassDecl) &&
		!(n instanceof DeclareParentsExt || n instanceof DeclareParentsImpl)) {
	    return this.bypassChildren(n);
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

    public QualifierNode disamb(QualifierNode tn) throws SemanticException {
	if (tn instanceof Ambiguous) {
	    QualifierNode qual;
	    String name;
	    if (tn instanceof AmbQualifierNode) {
		qual = ((AmbQualifierNode)tn).qual();
		name = ((AmbQualifierNode)tn).name();
	    } else if (tn instanceof AmbTypeNode) {
		qual = ((AmbTypeNode)tn).qual();
		name = ((AmbTypeNode)tn).name();
	    } else {
		throw new RuntimeException("Unexpected ambiguous node in declare parents: "+tn.getClass().getName());
	    }
	    qual = disamb(qual);
	    tn = (QualifierNode) nodeFactory().disamb().disambiguate((Ambiguous)tn, this, tn.position(), qual, name);
	}
	return tn;
    }
}
