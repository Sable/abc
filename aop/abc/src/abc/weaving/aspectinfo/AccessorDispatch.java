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

import polyglot.types.MethodInstance;
import polyglot.types.ClassType;

import soot.javaToJimple.Util;
import abc.aspectj.types.InterTypeFieldInstance_c;
import abc.aspectj.types.InterTypeMethodInstance;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.soot.util.MethodAccessorMethodSource;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ThisRef;
import soot.util.Chain;
import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel Avgustinov
 *
 * Class representing an accessor method for a field - given the appropriate information, it
 * constructs a public method in the appropriate class, with the appropriate type, taking
 * the same arguments as the method it's wrapping around and returning that method's result.
 */
public class AccessorDispatch extends AccessorMethod {
//    MethodInstance mi;
    MethodSig ms;
    
    public AccessorDispatch(String name, MethodInstance mi, ClassType target, Position pos) {
        super(name, target, pos);
        this.inst = mi;
        // If this is a method introduced by an ITD, then the name will be mangled - in fact, our
        // current method instance is useless, use the one that it was transformed to...
        if(mi instanceof InterTypeMethodInstance_c) {
            InterTypeMethodInstance imi = (InterTypeMethodInstance) mi;
            this.inst = imi.mangled();
        }
    }
    
    public void addSootMethod(int modifiers) {
        this.ms = AbcFactory.MethodSig((MethodInstance)inst);

        // From InterTypeAdjuster.addSuperDispatch() - specify what flags we want here, ignoring parameter (TODO)
        modifiers = ms.getModifiers();
        modifiers |= Modifier.PUBLIC;
		modifiers &= ~Modifier.PRIVATE;
		modifiers &= ~Modifier.PROTECTED;
		modifiers &= ~Modifier.NATIVE;
		modifiers &= ~Modifier.ABSTRACT;
		if(inst.flags().isStatic())
		    modifiers |= Modifier.STATIC;

        // TODO: Check if this is too expensive
        soot.FastHierarchy hierarchy = soot.Scene.v().getOrMakeFastHierarchy();
        
        soot.SootClass sc = AbcFactory.AbcClass(target).getSootClass();
        if(sc.isInterface()) {
            addDispatch(modifiers, sc, true);
            Set implementors = hierarchy.getAllImplementersOfInterface(sc);
            for(Iterator it = implementors.iterator(); it.hasNext(); ) {
                final soot.SootClass childClass = (soot.SootClass) it.next();
                if(childClass.isInterface()) continue;
                if(childClass.hasSuperclass() && implementors.contains(childClass.getSuperclass())) continue;
                
                addDispatch(modifiers, childClass, false);
            }
        }
        else addDispatch(modifiers, sc, false);
    }
    
    protected void addDispatch(int modifiers, soot.SootClass sc, boolean bAbstract) {
		if (bAbstract)
			modifiers |= Modifier.ABSTRACT;
        
        // This code adapted from soot.javaToJimple.initialResolver.handlePrivateAccessors().
        ArrayList paramTypes = new ArrayList();
        MethodInstance mi = (MethodInstance) inst;
        Iterator it = mi.formalTypes().iterator();
        while(it.hasNext()) {
            paramTypes.add(Util.getSootType((polyglot.types.Type)it.next()));
        }

        soot.Type returnType = Util.getSootType(mi.returnType());
        
        soot.SootMethod accessMeth = new soot.SootMethod(name, paramTypes, returnType, modifiers);
        
		for( Iterator exceptionIt = ms.getSootExceptions().iterator(); exceptionIt.hasNext(); ) {
			final SootClass exception = (SootClass) exceptionIt.next();
			accessMeth.addException( exception );
		}
        
        //TODO: Check if this is the right thing to omit for abstract methods
        if(!bAbstract) {
            MethodAccessorMethodSource mams = new MethodAccessorMethodSource(mi, sc);
        	accessMeth.setSource(mams);
        }
        
        sc.addMethod(accessMeth);
		
        registerMethod(accessMeth);
    }
    
    public void registerMethod(soot.SootMethod sm) {
        MethodCategory.register(sm, MethodCategory.INTERTYPE_SPECIAL_CALL_DELEGATOR);
    }
}
