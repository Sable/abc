
package abc.aspectj.types;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;

import polyglot.util.Position;

import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.ReferenceType;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;


import polyglot.ext.jl.types.MethodInstance_c;

public class PointcutInstance_c extends MethodInstance_c {

String localName;

	public PointcutInstance_c(TypeSystem ts, Position pos,
												ReferenceType container,
												Flags flags, Type returnType, String name,
												List formalTypes, List excTypes){
		super(ts,pos,container,flags,returnType,"$pointcut$"+name,formalTypes,excTypes);
		localName = name;
	}	
 	
	public String toString() {
	   String s = designator() + " " + flags.translate() +
					  signature();

	   return s;
	}
	
	public String signature() {
			String s = localName + "(";

			for (Iterator i = formalTypes.iterator(); i.hasNext(); ) {
				Type t = (Type) i.next();
				s += t.toString();

				if (i.hasNext()) {
					s += ",";
				}
			}

			s += ")";

			return s;
		}

	public String designator() {
		   return "pointcut";
	}
	
	
	public boolean isSameMethodImpl(MethodInstance mj) {
		if (mj instanceof PointcutInstance_c) {
			return name().equals(mj.name());
		} else return false;
	} 
	
	public List implementedImpl(ReferenceType rt) {
	   if (rt == null) {
		   return Collections.EMPTY_LIST;
	   }

		   List l = new LinkedList();
		   
		for (Iterator pci=rt.methodsNamed(name).iterator(); pci.hasNext(); ) {
			MethodInstance mi = (MethodInstance) pci.next();
			if (mi instanceof PointcutInstance_c)
				l.add(mi);
		}

	   Type superType = rt.superType();
	   if (superType != null) {
		   l.addAll(implementedImpl(superType.toReference())); 
	   }
	
	   List ints = rt.interfaces();
	   for (Iterator i = ints.iterator(); i.hasNext(); ) {
			   ReferenceType rt2 = (ReferenceType) i.next();
		   l.addAll(implementedImpl(rt2));
	   }
	
		   return l;
	  }

	public boolean canOverrideImpl(MethodInstance mj, boolean quiet) throws SemanticException {
		PointcutInstance_c mi = this;
		this.implemented();
		if (mi == mj)
			return true;
		if (!(mj instanceof PointcutInstance_c)) {
			if (quiet) return false;
			throw new SemanticException(mi + " in " + mi.container() + " cannot override method in " + mj.container());
		}
		if (mi.flags().isAbstract() && mj.flags().isAbstract()) {
			if (quiet) return false;
			throw new SemanticException( mi + " in " + mi.container() + " cannot override definition in " + mj.container() + " because both are abstract");
		}
		if (!mi.flags().isAbstract() && !mj.flags().isAbstract()) {
			if (quiet) return false;
			throw new SemanticException(mi + " in " + mi.container() + " cannot override definition in " + mj.container() + " because both are concrete");
		}
		if (!mi.hasFormals(mj.formalTypes())) {
			if (quiet) return false;
			throw new SemanticException(mi + " in " + mi.container() + " cannot override definition in " + mj.container() + " because parameter types differ");
	    }
		if (mi.flags().moreRestrictiveThan(mj.flags())) {
			if (quiet) return false;
				   throw new SemanticException(mi.signature() + " in " + mi.container() +
											   " cannot override " + 
											   mj.signature() + " in " + mj.container() + 
											   "; attempting to assign weaker " + 
											   "access privileges", 
											   mi.position());
		}
	    return true; 
	}
	
	
}
