/*
 * Created on Jul 21, 2004
 */
package abc.aspectj.visit;
import polyglot.ast.JL;

import polyglot.util.Position;
import polyglot.util.UniqueID;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Field;

import polyglot.types.ClassType;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.types.AJTypeSystem;

import abc.weaving.aspectinfo.AccessorGet;
import abc.weaving.aspectinfo.AccessorMethod;
import abc.weaving.aspectinfo.AccessorSet;
import abc.weaving.aspectinfo.AccessorDispatch;
import abc.weaving.aspectinfo.AccessorQualSpecial;

/**
 * @author pavel
 *
 * Base class for several classes that replace member accesses with calls to accessor methods,
 * which are generated. Two examples are references to super.{member} in advice code, and references
 * to members which would normally not be visible from privileged aspects (and nested classes/aspects).
 * 
 * We distinguish three types of accessor methods - getters, setters and dispatchers. Get/Set methods
 * are needed for the obvious operations on fields. Their return type is the type of the field, getter
 * methods take no arguments, setter methods take a single argument - the new value of the field. 
 * Dispatchers are a public wrapper around methods that wouldn't normally be visible. They take the same
 * arguments as the method they're wrapped around, and return the same type.
 * 
 * In this class, we maintain lists of the accessor methods of all three types, plus accessor methods for
 * qualified 'this' references.
 */
public class AccessorMethods {
    /**
     * Subclasses should override this to alter how names are generated. For example, for a "Get" accessor
     * method, the name will be a unique ID based on "get$" + tag() + "$" + {fieldname}.
     */
    protected String tag() {
        return "accessor"; 
    }
    
    protected AccessorMethod findExistingAccessor(List accessors, MemberInstance mi) {
        for(Iterator it = accessors.iterator(); it.hasNext(); ) {
            AccessorMethod am = (AccessorMethod)it.next();
            if(am.getMemberInstance().equalsImpl(mi)) return am;
        }
        return null;
    }
    
    List getters = new LinkedList();
    List setters = new LinkedList();
    List dispatchers = new LinkedList();
    List qualSpecials = new LinkedList();
    
    protected AccessorDispatch createDispatcherObject(String name, MethodInstance mi, ClassType target, Position pos) {
        return new AccessorDispatch(name, mi, target, pos);
    }
    
    public Call accessorDispatch(AJNodeFactory nf, AJTypeSystem ts, Call c, ClassType target, Expr targetThis) {
        String accessorName;
        MethodInstance mi = c.methodInstance();
        AccessorMethod am = (AccessorMethod)findExistingAccessor(dispatchers, mi);
        if(am != null && target.equals(am.getTarget())) {
            // we already have an accessor for that member
            accessorName = am.getName();
            am.addPosition(c.position());
        }
        else {
            accessorName = UniqueID.newID(tag() + "$" + c.name());
            dispatchers.add(createDispatcherObject(accessorName, mi, target, c.position()));
        }
        mi = mi.name(accessorName).container(target);
        if(target.flags().isInterface()) {
            mi = mi.flags(mi.flags().Abstract().clear(Flags.NATIVE));
        }
        if(c.methodInstance().flags().isStatic()) {
            mi = mi.flags(mi.flags().Static());
        }
        mi = mi.flags(mi.flags().Public().clearPrivate());
        if(targetThis == null)
            return c.name(accessorName).methodInstance(mi);
        else
            return c.target(targetThis).name(accessorName).methodInstance(mi);
    }
    
    protected AccessorGet createGetterObject(String name, FieldInstance fi, ClassType target, Position pos) {
        return new AccessorGet(name, fi, target, pos);
    }
    
    public Call accessorGetter(AJNodeFactory nf, AJTypeSystem ts, Field f, ClassType target, Expr targetThis) {
        String accessorName;
        FieldInstance fi = f.fieldInstance();
        AccessorMethod am = (AccessorMethod)findExistingAccessor(getters, fi);
        if(am != null && target.equals(am.getTarget())) {
            // we already have an accessor for that member
            accessorName = am.getName();
            am.addPosition(f.position());
        }
        else {
            accessorName = UniqueID.newID("get$" + tag() + "$" + f.name());
            getters.add(createGetterObject(accessorName, fi, target, f.position()));
        }
        
        // Now create the call that should replace the field reference in the AST
        Call c;
        if(targetThis == null)
            c = nf.Call(f.position(), f.target(), accessorName);
        else
            c = nf.Call(f.position(), targetThis, accessorName);
        Flags flags = Flags.PUBLIC;
        if(target.flags().isInterface()) {
            flags = flags.set(Flags.ABSTRACT);
        }
        if(f.flags().isStatic()) {
            flags = flags.set(Flags.STATIC);
        }
        MethodInstance mi = ts.methodInstance(f.position(), target, flags, fi.type(), accessorName, new LinkedList(), new LinkedList());
        c = (Call)c.methodInstance(mi).type(fi.type());
        
        return c;
    }
    
    protected AccessorSet createSetterObject(String name, FieldInstance fi, ClassType target, Position pos) {
        return new AccessorSet(name, fi, target, pos);
    }

    public Call accessorSetter(AJNodeFactory nf, AJTypeSystem ts, Field f, ClassType target, 
            Expr targetThis, Expr value) {
        String accessorName;
        FieldInstance fi = f.fieldInstance();
        AccessorMethod am = (AccessorMethod)findExistingAccessor(setters, fi);
        if(am != null && target.equals(am.getTarget())) {
            // we already have an accessor for that member
            accessorName = am.getName();
            am.addPosition(f.position());
        }
        else {
            accessorName = UniqueID.newID("set$" + tag() + "$" + f.name());
            setters.add(createSetterObject(accessorName, fi, target, f.position()));
        }
        
        // Now create the call that should replace the field reference in the AST
        Call c;
        if(targetThis == null) 
            c = nf.Call(f.position(), f.target(), accessorName, value);
        else
            c = nf.Call(f.position(), targetThis, accessorName, value);
        List argTypes = new ArrayList(); argTypes.add(fi.type());
        Flags flags = Flags.PUBLIC;
        if(target.flags().isInterface()) {
            flags = flags.set(Flags.ABSTRACT);
        }
        if(f.flags().isStatic()) {
            flags = flags.set(Flags.STATIC);
        }
        MethodInstance mi = ts.methodInstance(f.position(), target, flags, fi.type(), accessorName, argTypes, new LinkedList());
        c = (Call)c.methodInstance(mi).type(fi.type());
        
        return c;
    }
    
    protected AccessorQualSpecial createQualSpecialObject(String name, MethodInstance mi, ClassType target,
            		ClassType qualifier, Position pos, boolean qualThisNotSuper) {
        return new AccessorQualSpecial(name, mi, target, qualifier, pos, qualThisNotSuper);
    }
    
    public Call accessorQualSpecial(AJNodeFactory nf, AJTypeSystem ts, ClassType target, Expr targetThis, 
            ClassType qualifier) {
        return accessorQualSpecial(nf, ts, target, targetThis, qualifier, true);
    }
        public Call accessorQualSpecial(AJNodeFactory nf, AJTypeSystem ts, ClassType target, Expr targetThis, 
                ClassType qualifier, boolean qualThisNotSuper) {
        String qualName = qualifier.fullName().replace('.', '$');
        String accessorName = UniqueID.newID(qualName + (qualThisNotSuper ? "$this" : "$super"));
        Call c = nf.Call(targetThis.position(), targetThis, accessorName);
        MethodInstance mi = ts.methodInstance(targetThis.position(), target, Flags.PUBLIC, qualifier, accessorName, new ArrayList(), new ArrayList());
        qualSpecials.add(createQualSpecialObject(accessorName, mi, target, qualifier, c.position(), qualThisNotSuper));
        return (Call) c.methodInstance(mi).type(qualifier);
    }

    public List getAccessorGetters() {
        return getters;
    }
    
    public List getAccessorSetters() {
        return setters;
    }
    
    public List getAccessorDispatchers() {
        return dispatchers;
    }
    
    public List getQualSpecials() {
        return qualSpecials;
    }
    
    public void addAllSootMethods() {
        for(Iterator accIt = getters.iterator(); accIt.hasNext(); ) {
            AccessorMethod am = (AccessorMethod)accIt.next();
            am.addSootMethod();
        }
        for(Iterator accIt = setters.iterator(); accIt.hasNext(); ) {
            AccessorMethod am = (AccessorMethod)accIt.next();
            am.addSootMethod();
        }
        for(Iterator accIt = dispatchers.iterator(); accIt.hasNext(); ) {
            AccessorMethod am = (AccessorMethod)accIt.next();
            am.addSootMethod();
        }
        for(Iterator accIt = qualSpecials.iterator(); accIt.hasNext(); ) {
            AccessorMethod am = (AccessorMethod)accIt.next();
            am.addSootMethod();
        }
    }
}
