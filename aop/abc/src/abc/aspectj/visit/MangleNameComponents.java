/*
 * Created on 18-Aug-2004
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
 * @author oege
 */
public class MangleNameComponents extends NodeVisitor {


	/**
	 * 
	 */
	public MangleNameComponents() {
		super();
	}

	public NodeVisitor enter(Node n) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl) n;
			for (Iterator miIt = cd.type().methods().iterator(); miIt.hasNext(); ) {
				MethodInstance mi = (MethodInstance) miIt.next();
				if (mi instanceof InterTypeMemberInstance)
					((InterTypeMemberInstance) mi).setMangleNameComponent();
			}
		}
		return this;
	}
}

