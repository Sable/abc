
package abc.aspectj.visit;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;

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
		// System.out.println("**************************");
		ClassDecl cd = (ClassDecl) n;
		// System.out.println("class= "+cd.name());
		// System.out.println("methods="+cd.type().toClass().methods());
		// System.out.println("fields="+cd.type().toClass().fields());
		// System.out.println("constructors="+cd.type().toClass().constructors());
		
		if (cd.type().flags().isInterface())
			return this; // never apply ITDs to extends of interfaces
	
		 List methods = cd.type().methods();
		 List fields = cd.type().fields();
		 List constructors = cd.type().constructors();
		 Stack interfaces = new Stack();
		 Set visited = new HashSet();
		 interfaces.addAll(cd.type().interfaces());
		 while(!(interfaces.isEmpty())) {
		 	ClassType interf = ((ClassType) interfaces.pop());
		 	if (visited.contains(interf))
		 		continue;
		 	visited.add(interf);
		 	// System.out.println("processing interface "+interf);
		 	// does the super type of cd also implement this interface?
			// if so, we'll add it to the super type instead
	 	    if (cd.type().superType() != null)
				if (cd.type().superType().descendsFrom(interf)) 
					continue; 
			//	also add ITDS in the interfaces that interf extends
			// System.out.println(interf + " extends " + interf.interfaces());
		 	interfaces.addAll(interf.interfaces()); 
			for (Iterator mit = interf.methods().iterator(); mit.hasNext(); ) {
				 MethodInstance mi = (MethodInstance) mit.next();
				 // System.out.println("processing method "+ mi);
				 if (mi instanceof InterTypeMemberInstance) {
					// System.out.println("attempting to add method "+mi + " to " + cd.type() + " which implements " + interf +
					//	", which received method from " + ((InterTypeMemberInstance) mi).origin());
					abc.aspectj.ast.IntertypeMethodDecl_c.overrideITDmethod(cd.type(),
					           mi.container(cd.type()).flags(((InterTypeMemberInstance)mi).origFlags()));
				 	
				 }
			}
			for (Iterator fit = interf.fields().iterator(); fit.hasNext(); ) {
				FieldInstance fi = (FieldInstance) fit.next();
				// System.out.println("processing field "+fi);
				if (fi instanceof InterTypeMemberInstance) {
					// System.out.println("attempting to add field "+fi + " to " + cd.type() + " which implements " + interf + 
                     //   ", which received field from " +  ((InterTypeMemberInstance) fi).origin());
                    abc.aspectj.ast.IntertypeFieldDecl_c.overrideITDField(cd.type(),fi);
				}	
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
			System.out.println("******************************************");
			System.out.println("class= "+cd.name());
			System.out.println("methods="+cd.type().toClass().methods());
			System.out.println("fields="+cd.type().toClass().fields());
			System.out.println("constructors="+cd.type().toClass().constructors());
			
		}
		return n;
	}
	
*/


}
