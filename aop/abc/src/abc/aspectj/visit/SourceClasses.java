/*
 * Created on Jul 13, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.visit;

import polyglot.visit.NodeVisitor;
import polyglot.ast.Node;
import polyglot.ast.ClassDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcFactory;

/**
 * @author oege
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SourceClasses extends NodeVisitor {

	/**
	 * 
	 */
	public SourceClasses() {
		super();
		// TODO Auto-generated constructor stub
	}
	public NodeVisitor enter(Node n) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl) n;
			GlobalAspectInfo.v().registerSourceClass(AbcFactory.AbcClass(cd.type()));
		}
		return this;
	}

}
