/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.ast;

import polyglot.util.Position;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;

import polyglot.ast.Local;
import polyglot.ast.AmbExpr;
import polyglot.ast.Node;
import polyglot.ast.AmbTypeNode;
import polyglot.ast.TypeNode;
import polyglot.ast.Prefix;
import polyglot.ast.QualifierNode;

import polyglot.types.SemanticException;

import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.TypeChecker;
import polyglot.visit.TypeBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.types.UnknownType;

import polyglot.ext.jl.ast.Node_c;
import polyglot.ext.jl.ast.TypeNode_c;
import polyglot.ext.jl.ast.AmbTypeNode_c;

/** Represents either a type or a local. This is for arguments of <code>args(..)</code>,
 *  <code>this(..)</code>, <code>target(..)</code> as well as named pointcuts.
 *  Instances disambiguate to a Local or TypeNode.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class AmbTypeOrLocal_c extends ArgPattern_c implements AmbTypeOrLocal {
	TypeNode type; // an identifier that is an advice formal or a type
	
	public AmbTypeOrLocal_c(Position pos,TypeNode type) {
		super(pos);
		this.type = type;
	}
	
	/* return the typenode */ 
	public TypeNode type(){
		return type;
	}
	/** Disambiguate the expression. */
	 public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	 	// first try to produce a local that refers to an advice formal
	 	// System.out.println("disambiguating: "+type);
	 	if (type instanceof AmbTypeNode) {
	 		AmbTypeNode amb = (AmbTypeNode) type;
			// need to check it is actually an advice formal and not a field
	 		if (amb.qual()== null) { // if it has a qualifier it is a field
	 			// now build an AmbExpr with the right name
	 			AmbExpr ae = ar.nodeFactory().AmbExpr(position(),amb.name());
	 			// and try to resolve it...
				Node n = ar.nodeFactory().disamb().disambiguate(ae, ar, position(),
																		null, amb.name());
				if (n instanceof Local) { // the only locals visible in pointcuts are advice formals
               	         return n;
				}
	 		}
	 		// resolving to a local failed, so it must be a type
	 		/* Prefix pref = null;
	 		System.out.println("qualifier= "+amb.qual() + " of class "+amb.getClass());
	 		if (amb.qual() != null)
	 			pref = (Prefix) amb.qual().del().disambiguate(ar);
	 			amb.qual().del().disambiguateEnter(ar); */
			Node n = ar.nodeFactory().disamb().disambiguate(amb, ar, position(), amb.qual(),amb.name());
			// System.out.println("pref="+pref);
			if (n instanceof TypeNode) {
					return n;
			}
			throw new SemanticException("Could not find advice formal or type \"" + amb.name() +
										  "\". ", position());
	 	}
		return type;
	  }
	  
	protected AmbTypeOrLocal reconstruct(QualifierNode qual) {
		if (!(type instanceof AmbTypeNode))
			return this;
		AmbTypeNode atn = (AmbTypeNode) type;
	  	if (atn.qual() != qual) {
			AmbTypeNode_c n = (AmbTypeNode_c) atn.copy();
		type = n.qual(qual);
	  }

	  return this;
	}

	public Node visitChildren(NodeVisitor v) {
		if (!(type instanceof AmbTypeNode))
			return this;
		AmbTypeNode atn = (AmbTypeNode) type;
	  	QualifierNode qual = (QualifierNode) visitChild(atn.qual(), v);
	  	return reconstruct(qual);
	}
	
	  
	/** Type check the expression. */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
	  throw new InternalCompilerError(position(),
									  "Cannot type check ambiguous node "
									  + this + ".");
	} 

	/** Check exceptions thrown by the expression. */
	public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	  throw new InternalCompilerError(position(),
									  "Cannot exception check ambiguous node "
									  + this + ".");
	} 


	public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		if (type==null)
			w.write("*");
		else 
			print(type,w,tr);
	}
		   



}
