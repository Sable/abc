/*
 * Created on Oct 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.visit;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.AmbiguityRemover.Kind;

/**
 * @author oege
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AJAmbiguityRemover extends AmbiguityRemover {

	public static class AJKind extends Kind {
		  protected AJKind(String name) {
			  super(name);
		  }
	  }

	public static final Kind CLASSES = new AJKind("disam-classes");
	
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public AJAmbiguityRemover(
		Job arg0,
		TypeSystem arg1,
		NodeFactory arg2,
		Kind arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

}
