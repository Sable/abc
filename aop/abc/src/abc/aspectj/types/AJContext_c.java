
package abc.aspectj.types;

import polyglot.ext.jl.types.Context_c;

import polyglot.types.Context;
import polyglot.types.ParsedClassType;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.types.TypeSystem;

/**
 * @author Oege de Moor
 *
 * hostClass gives the host of an intertype decl
 * pushHost is called upon entering an intertype decl
 */
public class AJContext_c extends Context_c implements AJContext {
		
	protected ClassType host; // the host of the intertype decl
	protected boolean nested; // an inner class in an interType decl

	public AJContext_c(TypeSystem ts) {
		super(ts);
		host = null;
		nested = false;
	}
	
	public boolean inInterType() {
		return host != null;
	}
	
	public boolean nested() {
		return nested;
	}
	
	public ClassType hostClass() {
		return host;
	}
	
	public Context pushClass(ParsedClassType c, ClassType t) {
		AJContext_c r = (AJContext_c) super.pushClass(c,t);
		nested = inInterType();
		return r;
	}
	
	public Context pushHost(ClassType t) {
		AJContext_c c = (AJContext_c) super.push();
		c.host = t;
		nested = false;
		staticContext = true; 
		return c;
	}
	
	

}
