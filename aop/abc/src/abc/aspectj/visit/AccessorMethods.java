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

/** Container class for storing accessor methods related to an aspect.
 * 
 * AccessorDecl has an instance of this as a member.
 * 
 * We have several kinds of accessor methods - field getter and setter methods, method dispatch accessors,
 * and methods to access the this pointer of textually enclosing classes.
 * 
 * You can create an accessor method (or obtain an existing one, if it was created earlier) by calling
 * one of the <code>accessor*()</code> methods with the appropriate arguments. Most things are taken care
 * of automatically - methods are registered with MethodCategory using the right values etc. The only
 * thing that needs to be done is to invoke <code>addAllSootMethods()</code> from the inter-type adjuster
 * in order to insert the method bodies into the corresponding classes. 
 * 
 * @author Pavel Avgustinov
 */
public class AccessorMethods {
    /**
     * Subclasses should override this to alter how names are generated. For example, for a "Get" accessor
     * method, the name will be a unique ID based on "get$" + tag() + "$" + {fieldname}.
     * 
     * ...although this behaviour is somewhat deprecated, as all accessors are created in this class.
     * Perhaps we should have a "tag" argument to the accessor* calls, so that we can have
     * get$super$f() and get$privileged$f() - it's certainly not important, though.
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
