/* abc - The AspectBench Compiler
 * Copyright (C) 2004 pavel
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
 * Created on Jul 21, 2004
 */
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import polyglot.types.FieldInstance;
import polyglot.types.ClassType;
import soot.AbstractTrap;

import soot.javaToJimple.Util;
import abc.aspectj.types.InterTypeFieldInstance;
import abc.aspectj.types.InterTypeFieldInstance_c;
import abc.soot.util.FieldSetAccessorMethodSource;
import soot.Modifier;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author pavel
 *
 * Class representing an accessor method for a field - given the appropriate information, it
 * constructs a public method in the appropriate class, with the appropriate type, taking
 * a single argument and setting the appropriate field's value to that argument, returning the
 * new value.
 */
public class AccessorSet extends AccessorMethod {
//    FieldInstance fi;
    FieldSig fs;

    public AccessorSet (String name, FieldInstance fi, ClassType target, Position pos) {
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
        
        if(inst.flags().isStatic())
            modifiers |= Modifier.STATIC;
        
        ArrayList paramTypes = new ArrayList();
        soot.SootClass sc = AbcFactory.AbcClass(target).getSootClass();
        FieldSig field = AbcFactory.FieldSig(fi);
        
        soot.Type returnType = Util.getSootType(fi.type());
        paramTypes.add(returnType);
        
        soot.SootMethod accessMeth = new soot.SootMethod(name, paramTypes, returnType, modifiers);
        FieldSetAccessorMethodSource fsams = new FieldSetAccessorMethodSource(returnType, fi.name(), sc, fi.flags().isStatic());
        
        accessMeth.setSource(fsams);
        
        sc.addMethod(accessMeth);
        // not sure what this does:
        //accessMeth.setActiveBody(pfsms.getBody(meth, null));
        
        registerMethod(accessMeth);
    }
    
    public void registerMethod(soot.SootMethod sm) {
        MethodCategory.registerFieldSet(fs.getSootField(), sm);
    }
}
