
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

import polyglot.frontend.Pass;

import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.FieldInstance;
import polyglot.types.ConstructorInstance;

import polyglot.visit.NodeVisitor;
import polyglot.visit.ContextVisitor;

import abc.aspectj.ExtensionInfo;
import abc.aspectj.types.InterTypeMemberInstance;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcClass;

public class InterfaceITDs extends OncePass {


	public InterfaceITDs(Pass.ID id) {
	super(id);
	}
	
	public void once() {
		for (Iterator weavableClasses = GlobalAspectInfo.v().getWeavableClasses().iterator();
		 	weavableClasses.hasNext(); ) {
		 		ClassType ctype = ((AbcClass) weavableClasses.next()).getPolyglotType();
				if (ctype.flags().isInterface())
					continue;
				System.out.println("processing "+ctype);
				Stack interfaces = new Stack();
				Set visited = new HashSet();
				interfaces.addAll(ctype.interfaces());
				while(!(interfaces.isEmpty())) {
					ClassType interf = ((ClassType) interfaces.pop());
					if (visited.contains(interf))
								continue;
					visited.add(interf);
					System.out.println("ctype="+ctype+" intrf="+interf);
							
					// does the super type of ctype also implement this interface?
					// if so, we'll add it to the super type instead
					if (ctype.superType() != null)
						if (ctype.superType().descendsFrom(interf)) 
							continue; 
					//	also add ITDS in the interfaces that interf extends
					interfaces.addAll(interf.interfaces()); 
					for (Iterator mit = interf.methods().iterator(); mit.hasNext(); ) {
						MethodInstance mi = (MethodInstance) mit.next();
						if (mi instanceof InterTypeMemberInstance) {
							abc.aspectj.ast.IntertypeMethodDecl_c.overrideITDmethod(ctype,
											   mi.container(ctype).flags(((InterTypeMemberInstance)mi).origFlags()));
				 	
						}
					}
					for (Iterator fit = interf.fields().iterator(); fit.hasNext(); ) {
						FieldInstance fi = (FieldInstance) fit.next();
						if (fi instanceof InterTypeMemberInstance) {
							abc.aspectj.ast.IntertypeFieldDecl_c.overrideITDField(ctype,fi);
						}	
					}
					for (Iterator cit = interf.constructors().iterator(); cit.hasNext(); ) {
						ConstructorInstance ci = (ConstructorInstance) cit.next();
						if (ci instanceof InterTypeMemberInstance) {
							abc.aspectj.ast.IntertypeConstructorDecl_c.overrideITDconstructor(ctype,
											 ci.container(ctype).flags(((InterTypeMemberInstance)ci).origFlags()));
						}
					}
				}
		 	}
	}



}
