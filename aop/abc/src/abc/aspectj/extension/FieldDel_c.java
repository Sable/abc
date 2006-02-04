/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.util.InternalCompilerError;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;
import polyglot.types.ClassType;
import soot.javaToJimple.jj.ast.JjAccessField_c;

/**
 * @author Julian Tibble
 * @author Oege de Moor
 *
 */
public class FieldDel_c extends JL_c implements MakesAspectMethods
{
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        // do nothing
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        Field f = (Field) node();
        Call getter, setter; // to construct a JjAccessField, if necessary.

        if(ts.isAccessible(f.fieldInstance(), visitor.context()) &&
                !ts.isAccessibleIgnorePrivileged(f.fieldInstance(), visitor.context())) {
            ClassType cct = (ClassType) visitor.container(); // TODO: Check container() is what we want
            while(cct != null) {
                if(AJFlags.isPrivilegedaspect(cct.flags())) {
                    AspectType at = (AspectType) cct;
                    getter = at.getAccessorMethods().accessorGetter(nf, ts, f, (ClassType)f.target().type(), null);
                    setter = at.getAccessorMethods().accessorSetter(nf, ts, f, (ClassType)f.target().type(), null, (Expr)f);
                    Expr newf = new JjAccessField_c(f.position(), getter, setter, f);
                    return newf.type(f.type());
                }
                cct = cct.outer();
            }
            // Shouldn't happen - accessibility test thinks we're in a privileged aspect,
            // but we failed to find a containing aspect
            throw new InternalCompilerError("Problem determining whether or not we're in a privileged aspect");
        }

        if (f.fieldInstance() instanceof InterTypeFieldInstance_c) {
            InterTypeFieldInstance_c itfi = (InterTypeFieldInstance_c) f.fieldInstance();
            if (itfi.container().toClass().flags().isInterface()){
                getter = (Call)itfi.getCall(nf, ts, f.target(), itfi.container());
                setter = (Call)itfi.setCall(nf, ts, f.target(), itfi.container(), (Expr)f);
                Expr newf = new JjAccessField_c(f.position(), getter, setter, f);
                return newf.type(f.type());
            }
            f = f.fieldInstance(itfi.mangled()).name(itfi.mangled().name()).targetImplicit(false);
        }

        if (f.target() instanceof HostSpecial_c)
        {
            HostSpecial_c hs = (HostSpecial_c) f.target();
            if (hs.kind() == Special.SUPER) {
                IntertypeDecl id = (IntertypeDecl) visitor.intertypeDecl();
                /*return id.getSupers().superFieldGetter(nf, ts, f, id.host().type().toClass(),
                                                                  id.thisReference(nf, ts));*/
                    AspectType aspct = ((AJContext)visitor.context()).currentAspect();
                    if(aspct == null) {
                        // Is this really impossible? Depends on how exactly the nesting works, investigate
                        throw new InternalCompilerError("Intertype method not enclosed by aspect");
                    }
                    getter = aspct.getAccessorMethods().accessorGetter(nf, ts, f, id.host().type().toClass(),
                        id.thisReference(nf, ts));
                    setter = aspct.getAccessorMethods().accessorSetter(nf, ts, f, id.host().type().toClass(),
                        id.thisReference(nf, ts), (Expr)f);
                    Expr newf = new JjAccessField_c(f.position(), getter, setter, f);
                    return newf.type(f.type());
            }
        }
        return f;
    }
}
