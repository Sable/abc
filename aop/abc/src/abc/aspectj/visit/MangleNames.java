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
import abc.aspectj.types.AspectJTypeSystem;

/**
 * @author oege
 */
public class MangleNames extends NodeVisitor {
	
	AspectJTypeSystem ts;

	/**
	 * 
	 */
	public MangleNames(TypeSystem ts) {
		super();
		this.ts = (AspectJTypeSystem) ts;
	}

	public NodeVisitor enter(Node n) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl) n;
			for (Iterator miIt = cd.type().methods().iterator(); miIt.hasNext(); ) {
				MethodInstance mi = (MethodInstance) miIt.next();
				if (mi instanceof InterTypeMemberInstance)
					((InterTypeMemberInstance) mi).setMangle(ts);
			}
		}
		return this;
	}
}
