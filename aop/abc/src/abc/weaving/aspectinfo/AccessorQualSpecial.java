/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Pavel Avgustinov
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

package abc.weaving.aspectinfo;

import soot.SootMethod;
import polyglot.types.MethodInstance;
import polyglot.types.ClassType;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import java.util.ArrayList;

import abc.soot.util.QualSpecialAccessorMethodSource;
import soot.Modifier;

/**
 * @author Pavel Avgustinov
 */
public class AccessorQualSpecial extends AccessorMethod {
    MethodInstance mi;
    ClassType qualifier;
    boolean qualThisNotSuper;
    
    public AccessorQualSpecial(String name, MethodInstance mi, ClassType target,
            		ClassType qualifier, Position pos, boolean qualThisNotSuper) {
        super(name, target, pos);
        this.mi = mi;
        this.qualifier = qualifier;
        this.qualThisNotSuper = qualThisNotSuper;
        if(qualThisNotSuper == false) {
            throw new InternalCompilerError("Qualified super access not yet implemented");
        }
    }

    /* (non-Javadoc)
     * @see abc.weaving.aspectinfo.AccessorMethod#addSootMethod(int)
     */
    public void addSootMethod(int modifiers) {
        MethodSig method = AbcFactory.MethodSig(mi);
        ArrayList paramTypes = new ArrayList();
        soot.Type retType = method.getReturnType().getSootType();
        
        // Ignore modifiers - this must be public
        modifiers = Modifier.PUBLIC;
        
        soot.SootClass sc = AbcFactory.AbcClass(target).getSootClass();
        
        SootMethod sm = new SootMethod(method.getName(), paramTypes, retType, modifiers);
        
        QualSpecialAccessorMethodSource qsams = new QualSpecialAccessorMethodSource(method, sc, 
                    AbcFactory.AbcClass(qualifier).getSootClass(), qualThisNotSuper);
        sm.setSource(qsams);
        sc.addMethod(sm);
        
        registerMethod(sm);
    }

    /* (non-Javadoc)
     * @see abc.weaving.aspectinfo.AccessorMethod#registerMethod(soot.SootMethod)
     */
    public void registerMethod(SootMethod sm) {
        if(qualThisNotSuper) {
            MethodCategory.register(sm, MethodCategory.THIS_GET);
        }
    }

}
