
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
 */
public class AJContext_c extends Context_c implements AJContext {
		
	protected ClassType host; // the host of the intertype decl
	protected boolean nested; // an inner class in an interType decl
    protected boolean declaredStatic; // intertype decl declared static?
    protected AJContext_c startHostScope; // the first item on the context stack that signifies an ITD
    protected AJContext_c endHostScope; // the last item on the context stack that signifies an ITD
    protected ClassType fakeType;
    protected boolean indeclare;
    
	public AJContext_c(TypeSystem ts) {
		super(ts);
		host = null;
		nested = false;
		indeclare = false;
	}
	
	
	public AJContext pushDeclare() {
		AJContext_c c = (AJContext_c) super.push();
		c.indeclare = true;
		return c;
	}
	
	public boolean inDeclare() {
		return indeclare;
	}

	public ClassType aspect() {
		return startHostScope.currentClass();
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
		r.nested = inInterType();
		return r;
	}
	
	public Context pushHost(ClassType t, boolean declaredStatic) {
		AJContext_c c = (AJContext_c) super.push();
		c.host = t;
		c.nested = false;
		c.staticContext = true; 
		c.startHostScope = c;
		c.declaredStatic = declaredStatic;
		return c;
	}
	
	
	public Context pushStatic() {
		AJContext_c c = (AJContext_c) super.pushStatic();
		c.declaredStatic = true;
		return c;
	}
	
	public boolean explicitlyStatic() {
		return declaredStatic;
	}
	
	private Context fakePushClass(ClassType ct) {
		AJContext_c c = (AJContext_c) super.push();
		c.fakeType = ct;
		return c;
	}
	
	/**
		 * Finds the class which added a field to the scope.
		 * just like findFieldScope, but return fakeType rather than type
		 */
	public ClassType findFieldScopeInHost(String name)  {
		VarInstance vi = findVariableInThisScope(name);
		if (vi instanceof FieldInstance) {
			return fakeType;
		}
		if (vi == null && outer != null) {
			return ((AJContext_c)outer).findFieldScopeInHost(name);
		}
		throw new InternalCompilerError ("Field " + name + " not found.");
	}
	
	
	/** Finds the class which added a method to the host scope.
	   */
	  public ClassType findMethodScopeInHost(String name) {
		  ClassType container = findMethodContainerInThisScope(name);
		  if (container != null) {
			  return fakeType;
		  }
		  if (outer != null) {
			  return ((AJContext_c)outer).findMethodScopeInHost(name);
		  }
		  throw new InternalCompilerError ("Method " + name + " not found.");
	  }
	
	public void addMethodContainerToThisHostScope(MethodInstance mi) {
		 if (methods == null) methods = new HashMap();
		 methods.put(mi.name(), fakeType);
	 }

	
	public AJContext startHostScope() {
		return startHostScope;
	}
	
	public AJContext endHostScope() {
		return endHostScope;
	}
	
	private boolean varBetween(String name) {
		if (this == startHostScope)
			return false; /* the first host frame does not contain host members */
		else 
			return (findVariableInThisScope(name) != null ||
						((AJContext_c)outer).varBetween(name));
	}
	
	public boolean varInHost(String name) {
		if (this == startHostScope.endHostScope)
			 return varBetween(name);
		else
		    return findVariableInThisScope(name) == null && ((AJContext_c)outer).varInHost(name);
	}

	private boolean methodBetween(String name) {
			if (this == startHostScope)
				return false;
			else
				return (findMethodContainerInThisScope(name) != null ||
							((AJContext_c)outer).methodBetween(name));
	}
	
	public boolean methodInHost(String name) {
		if (this == startHostScope.endHostScope)
			return methodBetween(name);
		else
			return findMethodContainerInThisScope(name) == null && ((AJContext_c)outer).methodInHost(name);
	} 


	public AJContext addITMembers(ReferenceType type) {
		if (type !=	null)
		{
		   AJContext_c nc = addOuters(type);
		   endHostScope = nc;
		   return nc;
		}
		else return this;
	}
	
	private AJContext_c addOuters(ReferenceType type) {
		// add members of outer classes, in order of lexical scoping
			  // so first build up the list of outer classes, then traverse it
			  AJContext_c result = this;
			  if (type instanceof ParsedClassType) {
				   List outers = new LinkedList();
				   outers.add(hostClass());
				   ParsedClassType ct = (ParsedClassType) hostClass();
				   while (ct.outer() != null) {
					   outers.add(0,ct.outer());
					   ct = (ParsedClassType) ct.outer();
				   }
				   for (Iterator oct = outers.iterator(); oct.hasNext(); ) {
					   ct = (ParsedClassType) oct.next();
					   result = (AJContext_c) result.fakePushClass(ct);
					   result.addMembers(ct,new HashSet());
				   }
			  }
			  return result;
	}
	
	private void addMembers(ReferenceType type, Set visited) {

	   if (visited.contains(type)) {
		   return;
	   }
	
	   visited.add(type);
	
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
		   if (ts.isAccessible(mi,startHostScope)) 
				addMethodContainerToThisHostScope(mi); 
	   }
	
	   for (Iterator i = type.fields().iterator(); i.hasNext(); ) {
			FieldInstance fi = (FieldInstance) i.next();
			if (ts.isAccessible(fi,startHostScope)) 
				addVariable(fi);
	   }
	   
	   if (type.isClass()) {
				   for (Iterator i = type.toClass().memberClasses().iterator();
						i.hasNext(); ) {
					   ClassType mct = (ClassType) i.next();
					   if (ts.isAccessible(mct,startHostScope))
					   		addNamed(mct);
				   }
			   }
	}

}
