
package abc.aspectj.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import polyglot.util.InternalCompilerError;

import polyglot.ext.jl.types.Context_c;

import polyglot.types.Context;
import polyglot.types.ParsedClassType;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.MethodInstance;
import polyglot.types.VarInstance;
import polyglot.types.ReferenceType;
import polyglot.types.FieldInstance;
import polyglot.types.Type;

/**
 * @author Oege de Moor
 *
 * hostClass gives the host of an intertype decl
 * pushHost is called upon entering an intertype decl
 */
public class AJContext_c extends Context_c implements AJContext {
		
	protected ClassType host; // the host of the intertype decl
	protected boolean nested; // an inner class in an interType decl
    protected boolean declaredStatic; // intertype decl declared static?
    protected AJContext hostScope;
    
	public AJContext_c(TypeSystem ts) {
		super(ts);
		host = null;
		nested = false;
	}
	
	public boolean inInterType() {
		return host != null;
	}
	
	public boolean staticInterType() {
		return inInterType() && declaredStatic;
	}
	
	public boolean nested() {
		return nested;
	}
	
	public ClassType hostClass() {
		return host;
	}
	
	public Context pushClass(ParsedClassType c, ClassType t) {
		AJContext_c r = (AJContext_c) super.pushClass(c,t);
		r.nested = inInterType();
		return r;
	}
	
	public Context pushHost(ClassType t, boolean declaredStatic) {
		AJContext_c c = (AJContext_c) super.push();
		c.host = t;
		c.nested = false;
		c.declaredStatic = declaredStatic;
		c.staticContext = true; 
		c.hostScope = c;
		return c;
	}
	
	public AJContext hostScope() {
		return hostScope;
	}
	
	public boolean varInHost(String name) {
		if (this == hostScope)
			return findVariableInThisScope(name) != null;
		else
		    return findVariableInThisScope(name) == null && ((AJContext_c)outer).varInHost(name);
	}
	
	public boolean methodInHost(String name) {
		if (this == hostScope)
			return findMethodContainerInThisScope(name) != null;
		else
			return findMethodContainerInThisScope(name) == null && ((AJContext_c)outer).methodInHost(name);
	} 
	
	private void addITMethodHost(MethodInstance mi) {
		if (methods == null) methods = new HashMap();
	     // ITMs are always introduced into the scope by the host class, 
	     // not the current one
		methods.put(mi.name(), hostClass());
	}
	
	private void addITFieldHost(VarInstance var) {
		if (vars == null) vars = new HashMap();
		 vars.put(var.name(), var);
	}
	
	public void addITMembers(ReferenceType type) {
		if (type !=	null)
			addMembers(type,new HashSet());
	}
	
	private void addMembers(ReferenceType type, Set visited) {

	   if (visited.contains(type)) {
		   return;
	   }
	
	   visited.add(type);
	   
	   // add members of outer classes, in order of lexical scoping
	   // so first build up the list of outer classes, then traverse it
	   if (type instanceof ClassType) {
	   	 	List outers = new LinkedList();
	   	 	ClassType ct = type.toClass();
	   	 	while (ct.outer() != null) {
	   	 		outers.add(0,ct.outer());
	   	 		ct = ct.outer();
	   	 	}
	   	 	for (Iterator oct = outers.iterator(); oct.hasNext(); ) {
	   	 		ct = (ClassType) oct.next();
	   	 		addMembers(ct,visited);
	   	 	}
	   }
	
	   // Add supertype members first to ensure overrides work correctly.
	   if (type.superType() != null) {
		   if (! type.superType().isReference()) {
			   throw new InternalCompilerError(
				   "Super class \"" + type.superType() +
			   "\" of \"" + type + "\" is ambiguous.  " +
			   "An error must have occurred earlier.",
				   type.position());
		   }
	
		   addMembers(type.superType().toReference(), visited);
	   }
	
	   for (Iterator i = type.interfaces().iterator(); i.hasNext(); ) {
		   Type t = (Type) i.next();
	
		   if (! t.isReference()) {
			   throw new InternalCompilerError(
				   "Interface \"" + t + "\" of \"" + type +
			   "\" is ambiguous.  " +
			   "An error must have occurred earlier.",
				   type.position());
		   }
	
		   addMembers(t.toReference(),visited);
	   }
	   

	   
	
		AspectJTypeSystem ts = (AspectJTypeSystem) typeSystem();
	
	   for (Iterator i = type.methods().iterator(); i.hasNext(); ) {
		   MethodInstance mi = (MethodInstance) i.next();
		   if (ts.isAccessible(mi,this)) 
				addITMethodHost(mi); 
	   }
	
	   for (Iterator i = type.fields().iterator(); i.hasNext(); ) {
			FieldInstance fi = (FieldInstance) i.next();
			if (ts.isAccessible(fi,this)) 
				addITFieldHost(fi);
	   }
	   
	   if (type.isClass()) {
				   for (Iterator i = type.toClass().memberClasses().iterator();
						i.hasNext(); ) {
					   ClassType mct = (ClassType) i.next();
					   if (ts.isAccessible(mct,this))
					   		addNamed(mct);
				   }
			   }
	}

}
