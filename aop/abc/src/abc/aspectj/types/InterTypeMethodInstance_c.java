/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
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
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import polyglot.util.InternalCompilerError;

import polyglot.ext.jl.types.MethodInstance_c;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;

import polyglot.util.Position;
import polyglot.util.UniqueID;

/**
 * @author Oege de Moor
 */
public class InterTypeMethodInstance_c
	extends MethodInstance_c
	implements InterTypeMemberInstance, InterTypeMethodInstance {

	protected ClassType origin;
	protected MethodInstance mangled;
	protected Flags origFlags;
	protected ClassType interfaceTarget; // for interface ITDs
	protected String identifier;
	
	public ClassType origin() {
		return origin;
	}
	
	public Flags origFlags() {
		return origFlags;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	
	/**
	 * create a methodInstance for an intertype dedclaration that can
	 * be traced back to its origin.
	 */
	public InterTypeMethodInstance_c(
		TypeSystem ts,
		Position pos,
		String identifier,
		ClassType origin,
		ReferenceType container,
		Flags flags,
		Flags origFlags,
		Type returnType,
		String name,
		List formalTypes,
		List excTypes) {
		super(
			ts,
			pos,
			container,
			flags,
			returnType,
			name,
			formalTypes,
			excTypes);
		this.identifier = identifier;
		this.origin = origin;
		this.origFlags = origFlags;
		if (container.toClass().flags().isInterface())
			interfaceTarget = container.toClass();
		else
			interfaceTarget = null;
//		prepare for later transformation to mangled form:
		if (flags.isPrivate() || flags.isPackage()){
			Flags newFlags = flags.clearPrivate().set(Flags.PUBLIC);
			String origName = origin.toString().replace('.','$');
			String mangledName = UniqueID.newID(origName+"$"+name);
			mangled = flags(newFlags).name(mangledName);
		} else mangled = this;  // no mangling
	}
	
	public ClassType interfaceTarget() {
		return interfaceTarget;
	}

	/** fix up the mangled instance to agree with super type and interfaces. 
	 *   We employ a union-find structure for this purpose. From each itm instance,
	 *   the mangled name is propagated to the super type and interfaces. When
	 *  a previously set name is encountered, a union is performed to ensure all
	 *  related itm instances get the same mangled name.
	 *  */
	private class UnionFind {
		String name;
		UnionFind parent;
		
		UnionFind(String name) {
			this.name = name;
			parent = null;
		}
		
		// what component are we in?
		// may want to do path compression here, but probably not worth it
		UnionFind find() {
			if (parent==null) return this;
			else return parent.find();
		}
		
		// return the name of the constituent component
		String findName() {
			return find().name;
		}
		
		// union two components
		void union(UnionFind other) {
			UnionFind root = find();
			UnionFind otherRoot = other.find();
			if (root != otherRoot)
				root.parent = otherRoot;
		}
	}
	
	private UnionFind nameComponent = null;
	
	private void visit(UnionFind comp,Set visited) {
		if (visited.contains(this))
			return;
		visited.add(this);
		if (nameComponent !=null)
			nameComponent.union(comp);
		else
			nameComponent = comp;		
		// all the methods that this implements need to have the same name
		List followers = implemented();
		// and also if it overrides an abstract method in the superclass
		followers.addAll(overrides());
		AJTypeSystem_c ts = (AJTypeSystem_c) typeSystem();
		for (Iterator followIt = followers.iterator(); followIt.hasNext(); ) {
			MethodInstance mi = (MethodInstance) followIt.next();
			if (ts.isAccessible(mi,origin()) && mi instanceof InterTypeMethodInstance_c)
				((InterTypeMethodInstance_c)mi).visit(comp,visited);
		}
	}
	
	public void setMangleNameComponent() {
			UnionFind comp = new UnionFind(mangled().name());
			visit(comp, new HashSet());
		}
			
	public void setMangle() {
		mangled = mangled.name(nameComponent.findName());
	}
	
	public MethodInstance mangled() {
		return mangled;
	}

}
