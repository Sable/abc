/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Reehan Shaikh
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
package abc.ra.types;

import java.util.List;

import polyglot.ext.jl.types.MethodInstance_c;
import polyglot.frontend.Source;
import polyglot.main.Report;
import polyglot.types.Flags;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import abc.aspectj.types.AspectType;
import abc.eaj.types.EAJTypeSystem;
import abc.ra.ExtensionInfo;
import abc.tm.types.TMTypeSystem_c;

/**
 * Custom type system for relational aspects.
 *
 * @author Eric Bodden
 */
public class RATypeSystem_c extends TMTypeSystem_c implements EAJTypeSystem {
	
	/* (non-Javadoc)
	 * @see abc.ra.types.RATypeSystem#createAspectType(polyglot.types.LazyClassInitializer, polyglot.frontend.Source, int)
	 */
	public AspectType createAspectType(LazyClassInitializer init,Source fromSource,int perKind) {
		return new RelAspectType_c(this, init, fromSource, perKind);
	}
	
	public void checkTopLevelClassFlags(Flags f) throws SemanticException {
		//make type checker happy: "relational" flag is allowed
		super.checkTopLevelClassFlags(f.clear(ExtensionInfo.RELATIONAL_MODIFIER));
	}
	
	@Override
	public MethodInstance methodInstance(Position pos, ReferenceType container,
			Flags flags, Type returnType, String name, List argTypes,
			List excTypes) {
		return new MethodInstance_c(this, pos, container, flags, returnType, name, argTypes, excTypes) {
			
		    public boolean canOverrideImpl(MethodInstance mj, boolean quiet) throws SemanticException {
		        MethodInstance mi = this;

		        if (!(mi.name().equals(mj.name()) && mi.hasFormals(mj.formalTypes()))) {
		            if (quiet) return false;
		            throw new SemanticException("Arguments are different", mi.position());
		        }

/*
 * Have to disable the following typecheck because a relational aspect S can extend an abstract relational
 * aspect A. In this case, the method associate(..) in S has return type S but the one in A has type A. 
 * (covariant return types) 
 */ 		        
		        
//		        if (! ts.equals(mi.returnType(), mj.returnType())) {
//		            if (Report.should_report(Report.types, 3))
//		                Report.report(3, "return type " + mi.returnType() +
//		                              " != " + mj.returnType());
//		            if (quiet) return false;
//		            throw new SemanticException(mi.signature() + " in " + mi.container() +
//		                                        " cannot override " + 
//		                                        mj.signature() + " in " + mj.container() + 
//		                                        "; attempting to use incompatible " +
//		                                        "return type\n" +                                        
//		                                        "found: " + mi.returnType() + "\n" +
//		                                        "required: " + mj.returnType(), 
//		                                        mi.position());
//		        } 

		        if (! ts.throwsSubset(mi, mj)) {
		            if (Report.should_report(Report.types, 3))
		                Report.report(3, mi.throwTypes() + " not subset of " +
		                              mj.throwTypes());
		            if (quiet) return false;
		            throw new SemanticException(mi.signature() + " in " + mi.container() +
		                                        " cannot override " + 
		                                        mj.signature() + " in " + mj.container() + 
		                                        "; the throw set is not a subset of the " +
		                                        "overridden method's throw set", 
		                                        mi.position());
		        }   

		        if (mi.flags().moreRestrictiveThan(mj.flags())) {
		            if (Report.should_report(Report.types, 3))
		                Report.report(3, mi.flags() + " more restrictive than " +
		                              mj.flags());
		            if (quiet) return false;
		            throw new SemanticException(mi.signature() + " in " + mi.container() +
		                                        " cannot override " + 
		                                        mj.signature() + " in " + mj.container() + 
		                                        "; attempting to assign weaker " + 
		                                        "access privileges", 
		                                        mi.position());
		        }

		        if (mi.flags().isStatic() != mj.flags().isStatic()) {
		            if (Report.should_report(Report.types, 3))
		                Report.report(3, mi.signature() + " is " + 
		                              (mi.flags().isStatic() ? "" : "not") + 
		                              " static but " + mj.signature() + " is " +
		                              (mj.flags().isStatic() ? "" : "not") + " static");
		            if (quiet) return false;
		            throw new SemanticException(mi.signature() + " in " + mi.container() +
		                                        " cannot override " + 
		                                        mj.signature() + " in " + mj.container() + 
		                                        "; overridden method is " + 
		                                        (mj.flags().isStatic() ? "" : "not") +
		                                        "static", 
		                                        mi.position());
		        }

		        if (mi != mj && !mi.equals(mj) && mj.flags().isFinal()) {
			    // mi can "override" a final method mj if mi and mj are the same method instance.
		            if (Report.should_report(Report.types, 3))
		                Report.report(3, mj.flags() + " final");
		            if (quiet) return false;
		            throw new SemanticException(mi.signature() + " in " + mi.container() +
		                                        " cannot override " + 
		                                        mj.signature() + " in " + mj.container() + 
		                                        "; overridden method is final", 
		                                        mi.position());
		        }

		        return true;
		    }

			
		};
	}

	
}
