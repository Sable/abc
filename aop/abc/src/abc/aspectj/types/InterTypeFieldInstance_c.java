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

/*
 * Created on May 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import polyglot.util.Position;
import polyglot.util.UniqueID;

import polyglot.ast.Expr;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ast.Call;

import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;

import polyglot.ext.jl.types.FieldInstance_c;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.types.AJTypeSystem;

/**
 * @author Oege de Moor
 * a FieldInstance that was introduced via an intertype declaration, recording its origin
 */
public class InterTypeFieldInstance_c extends FieldInstance_c implements InterTypeMemberInstance, InterTypeFieldInstance {
	
	protected ClassType origin;
	protected FieldInstance mangled;
	protected MethodInstance getInstance;
	protected MethodInstance setInstance;
	protected String identifier;
		
	public String getIdentifier() {
		return identifier;
	}
	public InterTypeFieldInstance_c(TypeSystem ts, Position pos,
						String identifier,
						ClassType origin,
						ReferenceType container,
						Flags flags, Type type, String name) {
		 super(ts, pos, container, flags, type, name);
	 	this.origin = origin;
	 	this.identifier = identifier;
		//		prepare for later transformation to mangled form:
		if (flags.isPrivate() || flags.isPackage() || container.toClass().flags().isInterface()){
			Flags newFlags = flags.clearPrivate().set(Flags.PUBLIC);
			String originName = origin.name().replace('.','$');
			String mangledName = UniqueID.newID(originName + "$" + name);
			mangled = flags(newFlags).name(mangledName);
		} else mangled = this;  // no mangling
		// 		create setters and getters if needed
		if (container.toClass().flags().isInterface()) {
			Flags accessorFlags = Flags.PUBLIC.set(Flags.ABSTRACT);
			if (flags.isStatic())
				accessorFlags = accessorFlags.set(Flags.STATIC);
			List argTypes = new ArrayList();
			argTypes.add(type);
			String setname = UniqueID.newID("set$"+name);
			setInstance = ts.methodInstance(pos,container, accessorFlags,type,setname,argTypes, new ArrayList());
			String getname = UniqueID.newID("get$"+name);
			getInstance = ts.methodInstance(pos,container, accessorFlags,type,getname, new ArrayList(),new ArrayList());
			// mangled instance needs the same setters/getters.
			((InterTypeFieldInstance_c)mangled).getInstance = this.getInstance;
			((InterTypeFieldInstance_c)mangled).setInstance = this.setInstance;
		}
	 }
	
	public ClassType origin() {
		return origin;
	}
	
	public void setMangle() {
		// to be filled in!
	}
	
	public void setMangleNameComponent() {
	}

	public FieldInstance mangled() {
		return mangled;
	}
	
	public MethodInstance getGet() {
		return getInstance;
	}
	
	public MethodInstance getSet() {
		return setInstance;
	}
	
	public Flags origFlags() {
		return flags();
	}
	
	public Expr getCall(AJNodeFactory nf, AJTypeSystem ts, Receiver target, ReferenceType container) {
		Call c = nf.Call(position,target,getInstance.name());
		MethodInstance mi = getInstance.container(container);
		return c.methodInstance(mi).type(type());
	}
	
	public Expr setCall(AJNodeFactory nf, AJTypeSystem ts, Receiver target, ReferenceType container, 
							 Expr arg) {
		Call c = nf.Call(position,target,setInstance.name(),arg);
		MethodInstance mi = setInstance.container(container);
		return c.methodInstance(mi).type(type());
	}
	
	
	
}
