/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Pavel Avgustinov
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

package abc.aspectj.ast;

/**
 * Method instance that allows overriding with covariant return types.
 * This is just a hack to make it possible to compile java1.4 programs
 * with the java5 api, which uses covariant returns for Appendable.
 * 
 * @author Pavel Avgustinov
 */

import java.util.List;

import polyglot.ext.jl.types.MethodInstance_c;
import polyglot.main.Report;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class CovariantRetTypeMethodInstance_c extends MethodInstance_c
		implements MethodInstance {

	public CovariantRetTypeMethodInstance_c(TypeSystem ts, Position pos, ReferenceType container, Flags flags, Type returnType, String name, List formalTypes, List excTypes) {
		super(ts, pos, container, flags, returnType, name, formalTypes, excTypes);
	}

	/**
     * It's unfortunate this has to be mostly copied verbatim from Polyglot...
     * 
     * @param quiet If true, then no Semantic Exceptions will be thrown, and the
     *              return value will be true or false. Otherwise, if the method
     *              cannot override, then a SemanticException will be thrown, else
     *              the method will return true.
     */
    public boolean canOverrideImpl(MethodInstance mj, boolean quiet) throws SemanticException {
        MethodInstance mi = this;

        if (!(mi.name().equals(mj.name()) && mi.hasFormals(mj.formalTypes()))) {
            if (quiet) return false;
            throw new SemanticException("Arguments are different", mi.position());
        }

        if (! (mi.returnType().descendsFrom(mj.returnType()) || ts.equals(mi.returnType(), mj.returnType()))) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, "return type " + mi.returnType() +
                              " != " + mj.returnType());
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in " + mi.container() +
                                        " cannot override " + 
                                        mj.signature() + " in " + mj.container() + 
                                        "; attempting to use incompatible " +
                                        "return type\n" +                                        
                                        "found: " + mi.returnType() + "\n" +
                                        "required: " + mj.returnType(), 
                                        mi.position());
        } 

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
    
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	
}
