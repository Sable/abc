/*
 * Created on Jun 9, 2004
 */
package abc.aspectj.visit;

import java.util.Iterator;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;

import polyglot.types.MethodInstance;
import polyglot.types.TypeSystem;

import polyglot.visit.NodeVisitor;

import abc.aspectj.types.InterTypeMemberInstance;
import abc.aspectj.types.AJTypeSystem;

/**
 * @author Oege de Moor
 */
public class MangleNames extends NodeVisitor {

	/**
	 * 
	 */
	public MangleNames() {
		super();
	}

	public NodeVisitor enter(Node n) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl) n;
			for (Iterator miIt = cd.type().methods().iterator(); miIt.hasNext(); ) {
				MethodInstance mi = (MethodInstance) miIt.next();
				if (mi instanceof InterTypeMemberInstance)
					((InterTypeMemberInstance) mi).setMangle();
			}
		}
		return this;
	}
}
