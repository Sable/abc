
package abc.aspectj.ast;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import polyglot.util.Position;

import polyglot.ast.Call;
import polyglot.ast.Receiver;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;

import polyglot.visit.TypeChecker;

import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;

import polyglot.ext.jl.ast.Call_c;

import abc.aspectj.ast.AspectJNodeFactory;

import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.types.AJContext;

/**
 * Override the typechecking of method calls, to delegate to the host in certain
 * cases when the call occurs from within an intertype declaration.
 * 
 * @author oege
 *
 */
public class AJCall_c extends Call_c implements Call {
	
	public AJCall_c(Position pos, Receiver target, String name,
				  List arguments) {
	  	super(pos,target,name,arguments);
	}

	/**
   	* Typecheck the Call when the target is null. This method finds
   	* an appropriate target, and then type checks accordingly.
   	* 
   	* @param argTypes list of <code>Type</code>s of the arguments
   	*/
  	protected Node typeCheckNullTarget(TypeChecker tc, List argTypes) throws SemanticException {
	  AspectJTypeSystem ts = (AspectJTypeSystem) tc.typeSystem();
	  AspectJNodeFactory nf = (AspectJNodeFactory) tc.nodeFactory();
	  AJContext c = (AJContext) tc.context();

	  // the target is null, and thus implicit
	  // let's find the target, using the context, and
	  // set the target appropriately, and then type check
	  // the result
	  MethodInstance mi =  c.findMethod(this.name, argTypes);
    
	  Receiver r;
	  
	  if (mi.flags().isStatic()) {
	  	r = nf.CanonicalTypeNode(position(), mi.container()).type(mi.container());
	  } else // test whether this a call to an instance method of an ITHost 
	  		if (ts.refHostOfITD(c,null,mi)) {
	  			TypeNode tn = nf.CanonicalTypeNode(position(),c.hostClass()).type(c.hostClass());
	  			r = nf.hostSpecial(position(),Special.THIS,tn).type(c.hostClass());
	  		} else {
			  // The method is non-static, so we must prepend with "this", but we
			  // need to determine if the "this" should be qualified.  Get the
			  // enclosing class which brought the method into scope.  This is
			  // different from mi.container().  mi.container() returns a super type
			  // of the class we want.
			  ClassType scope = c.findMethodScope(name);
	
			  if (! ts.equals(scope, c.currentClass())) {
				  r = nf.This(position(),
							  nf.CanonicalTypeNode(position(), scope)).type(scope);
			  }
			  else {
				  r = nf.This(position()).type(scope);
			  }
	  }

	  // we call typeCheck on the receiver too.
	  r = (Receiver)r.typeCheck(tc);
	  return this.targetImplicit(true).target(r).typeCheck(tc);
  }

}
