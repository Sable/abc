/*
 * Created on May 6, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.ast;

import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ast.Special.Kind;
import polyglot.ast.Node;

import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.visit.TypeChecker;


import polyglot.ext.jl.ast.Special_c;
import polyglot.util.Position;

import polyglot.types.Type;

import abc.aspectj.types.AJContext;

/**
 * @author oege
 * specials in intertype declarations
 */
public class HostSpecial_c extends Special_c implements Special {

	Type host;
	
	public HostSpecial_c(Position pos, Kind kind, TypeNode qualifier,Type host) {
		super(pos, kind, qualifier);
		this.host = host;
	}
	

	/** Type check the expression. */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		   TypeSystem ts = tc.typeSystem();
		   AJContext c = (AJContext) tc.context();

		   ClassType t;

		   if (qualifier == null) {
			   // an unqualified "this" or "super"
			   t = c.hostClass(); // use the hostClass rather than currentClass
		   }
		   else {    
		   if (! qualifier.type().isClass()) {
		   throw new SemanticException("Qualified " + kind +
			   " expression must be of a class type",
			   qualifier.position());
		   }

			   t = qualifier.type().toClass();

			   if (!c.hostClass().hasEnclosingInstance(t)) {
				   throw new SemanticException("The nested class \"" + 
							   c.hostClass() + "\" does not have " +
							   "an enclosing instance of type \"" +
							   t + "\".", qualifier.position());
			   }
		   }

		if (c.explicitlyStatic() && ts.equals(t, c.hostClass())) {
			   // trying to access "this" or "super" from a static context.
			   throw new SemanticException("Cannot access a non-static " +
				   "field or method, or refer to \"this\" or \"super\" " + 
				   "from a static context.",  this.position());
		   }

	   if (kind == THIS) {
		   return type(t);
	   }
	   else if (kind == SUPER) {
		   return type(t.superType());
	   }
		   return this;
	   }

	/** Write the expression to an output file. */
	  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	  if (qualifier != null) {
		  print(qualifier, w, tr);
		  w.write(".");
	  }

	  w.write("host"+kind.toString());
	  }
}
