
package abc.aspectj.visit;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import polyglot.ast.NodeFactory;
import polyglot.ast.Node;
import polyglot.ast.ClassDecl;
import polyglot.ast.TypeNode;

import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.FieldInstance;
import polyglot.types.ConstructorInstance;

import polyglot.visit.NodeVisitor;

import abc.aspectj.types.InterTypeMemberInstance;

/**
 * Visitor that injects ITDs on interfaces into the classes that implement them.
 * @author oege
 */
public class InterfaceITDs extends NodeVisitor {

	public InterfaceITDs() {
		super();
	}
	
	public NodeVisitor enter(Node n) {
	 if (n instanceof ClassDecl) {
		 ClassDecl cd = (ClassDecl) n;
		 List methods = cd.type().methods();
		 List fields = cd.type().fields();
		 List constructors = cd.type().constructors();
		 for (Iterator inIt = cd.interfaces().iterator(); inIt.hasNext(); ) {
		 	ClassType interf = ((TypeNode) inIt.next()).type().toClass();
			for (Iterator mit = interf.methods().iterator(); mit.hasNext(); ) {
				 MethodInstance mi = (MethodInstance) mit.next();
				 if (mi instanceof InterTypeMemberInstance) 
				 	methods.add(mi.container(cd.type()));
			}
			for (Iterator fit = interf.fields().iterator(); fit.hasNext(); ) {
				FieldInstance fi = (FieldInstance) fit.next();
				if (fi instanceof InterTypeMemberInstance)
					fields.add(fi);
			}
			for (Iterator cit = interf.constructors().iterator(); cit.hasNext(); ) {
				ConstructorInstance ci = (ConstructorInstance) cit.next();
				if (ci instanceof InterTypeMemberInstance)
					constructors.add(ci);
			}
		 }
	 }
	 return this;
	}


}
