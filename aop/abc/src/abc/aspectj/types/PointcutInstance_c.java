/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.aspectj.types;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
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

/**
 * 
 * @author Oege de Moor
 *
 */
public class PointcutInstance_c extends MethodInstance_c {

    String localName;
    
    Set refersTo; /* PointcutInstance_c */
    Set transRefs;
    boolean transComputed;
    boolean transAbstract;
    boolean dynamic; /* does this pointcut require dynamic tests? */
    boolean transDynamicComputed;
    boolean transDynamic;

	public PointcutInstance_c(TypeSystem ts, Position pos,
												ReferenceType container,
												Flags flags, Type returnType, String name,
												List formalTypes, List excTypes){
		super(ts,pos,container,flags,returnType,"$pointcut$"+name,formalTypes,excTypes);
		localName = name;
		refersTo = null;
		transRefs = null;
		transComputed = false;
		transDynamicComputed = false;
	}	
 	
	public String toString() {
	   String s = designator() + " " + flags.translate() +
					  signature();

	   return s;
	}
	
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	
	public boolean isDynamic() {
		return dynamic;
	}
	
	public void setRefersTo(Set x) {
		refersTo = x;
	}
	
	public Set getRefersTo() {
		return refersTo;
	}
	
	public Set transRefs() {
		if (transRefs==null) {
			if (refersTo==null)
				System.out.println(this);
			transRefs = new HashSet(refersTo);
			for (Iterator refs = refersTo.iterator(); refs.hasNext(); ) {
				PointcutInstance_c ref = (PointcutInstance_c) refs.next();
				transRefs.addAll(ref.transRefs());
			}
			return transRefs;
		} else return transRefs;
	}
	
	public boolean transAbstract() {
		if (!transComputed) {
			transAbstract = flags().isAbstract();
			for (Iterator refs = transRefs().iterator(); refs.hasNext(); ) {
				PointcutInstance_c ref = (PointcutInstance_c) refs.next();
				transAbstract |= ref.flags().isAbstract();
			}
			transComputed = true;
			return transAbstract;
		} else return transAbstract;
	}
	
	public boolean transDynamic() {
		if (!transDynamicComputed) {
			transDynamic = isDynamic();
			for (Iterator refs = transRefs().iterator(); refs.hasNext(); ) {
				PointcutInstance_c ref = (PointcutInstance_c) refs.next();
				transDynamic |= ref.isDynamic();
			}
			transDynamicComputed = true;
			return transDynamic;
		} else return transDynamic;
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
		/* Aske believed this to be a rule, but the test in new/EmptyStack.java seems to indicate otherwise
		if (!mi.flags().isAbstract() && !mj.flags().isAbstract()) {
			if (quiet) return false;
			throw new SemanticException(mi + " in " + mi.container() + " cannot override definition in " + mj.container() + " because both are concrete");
		} */
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
