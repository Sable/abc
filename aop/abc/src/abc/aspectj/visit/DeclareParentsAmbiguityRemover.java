/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

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

import abc.aspectj.ast.DeclareParents;

/** Perform disambiguation of the parent classes of <code>declare parents</code>
 *  declarations.
 *  @author Aske Simon Christensen
 */
public class DeclareParentsAmbiguityRemover extends ContextVisitor {

    public DeclareParentsAmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected NodeVisitor enterCall(Node n) throws SemanticException {
	if (n instanceof ClassMember && !(n instanceof ClassDecl) &&
		!(n instanceof DeclareParents)) {
	    return this.bypassChildren(n);
	} 
	return this;
    }

    protected Node leaveCall(Node n) throws SemanticException {
	if (n instanceof DeclareParents) {
	    return ((DeclareParents)n).disambiguate(this);
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
		throw new polyglot.util.InternalCompilerError("Unexpected ambiguous node in declare parents: "+tn.getClass().getName());
	    }
	    qual = disamb(qual);
	    tn = (QualifierNode) nodeFactory().disamb().disambiguate((Ambiguous)tn, this, tn.position(), qual, name);
	}
	return tn;
    }
}
