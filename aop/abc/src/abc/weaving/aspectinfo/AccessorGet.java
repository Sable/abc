/*
 * Created on Jul 21, 2004
 */
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import polyglot.types.FieldInstance;
import polyglot.types.ClassType;

import soot.javaToJimple.Util;
import abc.aspectj.types.InterTypeFieldInstance_c;
import abc.soot.util.FieldGetAccessorMethodSource;
import soot.Modifier;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author pavel
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
            InterTypeFieldInstance_c ifi = (InterTypeFieldInstance_c) fi;
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
