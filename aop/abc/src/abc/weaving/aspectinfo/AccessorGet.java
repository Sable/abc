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

import polyglot.util.Position;

import polyglot.types.FieldInstance;
import polyglot.types.ClassType;

import soot.javaToJimple.Util;
import abc.aspectj.types.InterTypeFieldInstance;
import abc.aspectj.types.InterTypeFieldInstance_c;
import abc.soot.util.FieldGetAccessorMethodSource;
import soot.Modifier;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Pavel Avgustinov
 *
 * Class representing an accessor method for a field - given the appropriate information, it
 * constructs a public method in the appropriate class, with the appropriate type, taking
 * no arguments and returning the value of the appropriate field.
 */
public class AccessorGet extends AccessorMethod {
//    FieldInstance fi;
    FieldSig fs;
    
    public AccessorGet(String name, FieldInstance fi, ClassType target, Position pos){
        super(name, target, pos);
        this.inst = fi;
        // If this is a field introduced by an ITD, then the name will be mangled - in fact, our
        // current field instance is useless, use the one that it was transformed to...
        if(fi instanceof InterTypeFieldInstance_c) {
            InterTypeFieldInstance ifi = (InterTypeFieldInstance) fi;
            this.inst = ifi.mangled();
        }
    }
 
    public void addSootMethod(int modifiers) {
        FieldInstance fi = (FieldInstance)inst;
        this.fs = AbcFactory.FieldSig(fi);
        
        if(fi.flags().isStatic())
            modifiers |= Modifier.STATIC;
        
        // This code adapted from soot.javaToJimple.initialResolver.handlePrivateAccessors().
        ArrayList paramTypes = new ArrayList();
        soot.SootClass sc = AbcFactory.AbcClass(target).getSootClass();
        
        soot.Type returnType = Util.getSootType(fi.type());
        
        soot.SootMethod accessMeth = new soot.SootMethod(name, paramTypes, returnType, modifiers);
        
        FieldGetAccessorMethodSource fgams = new FieldGetAccessorMethodSource(Util.getSootType(fi.type()), fi.name(), sc, fi.flags().isStatic());
        accessMeth.setSource(fgams);
        
        sc.addMethod(accessMeth);
        
        registerMethod(accessMeth);
    }
    
    public void registerMethod(soot.SootMethod sm) {
        MethodCategory.registerFieldGet(fs.getSootField(), sm);
    }
}
