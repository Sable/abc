
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
		 for (Iterator inIt = cd.type().interfaces().iterator(); inIt.hasNext(); ) {
		 	ClassType interf = ((ClassType) inIt.next());
			for (Iterator mit = interf.methods().iterator(); mit.hasNext(); ) {
				 MethodInstance mi = (MethodInstance) mit.next();
				 if (mi instanceof InterTypeMemberInstance) 
				 	methods.add(mi.container(cd.type()).flags(((InterTypeMemberInstance)mi).origFlags()));
			}
			for (Iterator fit = interf.fields().iterator(); fit.hasNext(); ) {
				FieldInstance fi = (FieldInstance) fit.next();
				if (fi instanceof InterTypeMemberInstance)
					fields.add(fi);
			}
			for (Iterator cit = interf.constructors().iterator(); cit.hasNext(); ) {
				ConstructorInstance ci = (ConstructorInstance) cit.next();
				if (ci instanceof InterTypeMemberInstance)
					constructors.add(ci.container(cd.type()));
			}
		 }
	 }
	 return this;
	}
	
/*
	public Node leave(Node old, Node n, NodeVisitor v) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl) n;
			System.out.println("class= "+cd.name());
			System.out.println("methods="+cd.type().toClass().methods());
			System.out.println("fields="+cd.type().toClass().fields());
			System.out.println("constructors="+cd.type().toClass().constructors());
			
		}
		return n;
	}
	*/



}
