/*
 * Created on Jul 13, 2004
 *
 */
package abc.aspectj.visit;

import polyglot.visit.NodeVisitor;
import polyglot.ast.Node;
import polyglot.ast.ClassDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcFactory;

/**
 * @author Oege de Moor
 *
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
