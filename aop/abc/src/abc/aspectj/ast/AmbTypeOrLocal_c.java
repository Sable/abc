
package abc.aspectj.ast;

import polyglot.util.Position;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;

import polyglot.ast.Local;
import polyglot.ast.AmbExpr;
import polyglot.ast.Node;
import polyglot.ast.AmbTypeNode;
import polyglot.ast.TypeNode;

import polyglot.types.SemanticException;

import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.TypeChecker;

import polyglot.ext.jl.ast.Node_c;
import polyglot.ext.jl.ast.TypeNode_c;


public class AmbTypeOrLocal_c extends Node_c implements AmbTypeOrLocal {
	TypeNode type; // an identifier that is an advice formal, or a type, or *
	                             // * is initially represented by type==null
	                             // disambiguation makes it a TPEUniversal
	
	public AmbTypeOrLocal_c(Position pos,TypeNode type) {
		super(pos);
		this.type = type;
	}
	
	/** Disambiguate the expression. */
	 public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	 	// first try to produce a local that refers to an advice formal
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
			Node n = ar.nodeFactory().disamb().disambiguate(amb, ar, position(), amb.qual(),
															                                           amb.name());
			 if (n instanceof TypeNode) {
				return n;
			 }
			 throw new SemanticException("Could not find advice formal or type \"" + amb.name() +
										  "\". ", position());
	 	}
	 	if (type != null)
	 	    return type;
	 	 else return ((AspectJNodeFactory) ar.nodeFactory()).TPEUniversal(position());
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
