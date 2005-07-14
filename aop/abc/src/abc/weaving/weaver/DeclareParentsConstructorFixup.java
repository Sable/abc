/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.weaving.weaver;

import abc.weaving.aspectinfo.GlobalAspectInfo;

import polyglot.types.SemanticException;
import polyglot.util.Position;

import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.internal.JSpecialInvokeExpr;

import java.util.*;

/** Fixes up the superclass constructor calls in binary classes
 *  whose superclass have been changed by declare parents.
 *  @author Aske Simon Christensen
 */
public class DeclareParentsConstructorFixup {

    public void weave() throws SemanticException {
        Iterator eci = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getExtendedClasses().iterator();
        while (eci.hasNext()) {
            SootClass ec = (SootClass)eci.next();
            SootClass ecsuper = ec.getSuperclass();

            // Look through all constructors
            Iterator coni = ec.getMethods().iterator();
            while (coni.hasNext()) {
                SootMethod con = (SootMethod)coni.next();
                if (con.getName().equals("<init>")) {
                    // Find the super of this call
                    InvokeStmt callstmt = abc.soot.util.Restructure.findInitStmt(con.retrieveActiveBody().getUnits());
                    SpecialInvokeExpr call = (SpecialInvokeExpr)callstmt.getInvokeExpr();
                    SootMethod target = call.getMethod();
                    SootClass targetc = target.getDeclaringClass();
                    if (!(targetc.equals(ec) || targetc.equals(ecsuper))) {
                        // This is neither a this call nor a proper super call. It needs fixing.
                        if (ecsuper.declaresMethod("<init>", target.getParameterTypes())) {
                            // Matching constructor exists in new parent. Use it.
                            SootMethodRef par_constr = Scene.v().makeConstructorRef(ecsuper, target.getParameterTypes());
                            SpecialInvokeExpr newcall = new JSpecialInvokeExpr((Local)call.getBase(),
                                                                               par_constr,
                                                                               call.getArgs());
                            callstmt.setInvokeExpr(newcall);
                        } else {
                            Position pos = abc.polyglot.util.ErrorInfoFactory.getPosition(con, callstmt);
                            throw new SemanticException("No constructor in declared parent matches super call", pos);
                        }
                    }
                }
            }
        }
    }

}
