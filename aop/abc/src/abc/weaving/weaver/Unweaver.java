/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Jimple;
import abc.weaving.aspectinfo.AbcClass;

/** Saves all method bodies so that they can be unwoven after weaving.
 * @author Ondrej Lhotak
 * @author Eric Bodden
 * @date August 3, 2004
 */

public class Unweaver {
    private static void debug(String message) {
        if (abc.main.Debug.v().unweaver) 
            System.err.println("UNWEAVER ***** " + message);
    }	

    protected Map<SootMethod, Body> savedBodies;
    protected Map<SootMethod, List<Type>> savedParameters;
    protected Map<SootClass, Collection<SootMethod>> classToMethods;
    protected Map<SootClass, Collection<SootField>> classToFields;
    protected Set<SootClass> applicationClasses;
    protected Map<SootClass, HashSet<SootClass>> classToInterfaces;
    
    /** Save Jimple bodies of all weavable classes to be restored later. */
    public void save() {
        savedBodies = new HashMap<SootMethod, Body>();
        classToMethods = new HashMap<SootClass, Collection<SootMethod>>();
        classToFields = new HashMap<SootClass, Collection<SootField>>();
        savedParameters = new HashMap<SootMethod, List<Type>>();
        classToInterfaces = new HashMap<SootClass, HashSet<SootClass>>();
        
        applicationClasses=
        	new HashSet<SootClass>(Scene.v().getApplicationClasses());
        
        for( Iterator<AbcClass> abcClassIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); abcClassIt.hasNext(); ) {
            final AbcClass abcClass = abcClassIt.next();
            SootClass cl = abcClass.getSootClass();
            classToMethods.put( cl, new HashSet<SootMethod>() );
            classToFields.put( cl, new HashSet<SootField>() );
            classToInterfaces.put(cl, new HashSet<SootClass>(cl.getInterfaces()) );
            
            debug( "saving "+cl );
            for( Iterator<SootMethod> mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = mIt.next();
                if( m.hasActiveBody() ) {
                    savedBodies.put( m, m.getActiveBody() );
                }
                savedParameters.put(m, m.getParameterTypes());
                classToMethods.get(cl).add(m);
            }
            for( Iterator<SootField> fIt = cl.getFields().iterator(); fIt.hasNext(); ) {
                final SootField f = fIt.next();
                classToFields.get(cl).add(f);
            }
            
        }
    }

    /** Restore saved bodies to their original methods. */
    public Map<Object,Object> restore() {
        Map<Object, Object> ret = new HashMap<Object, Object>();
        for( Iterator<SootMethod> mIt = savedBodies.keySet().iterator(); mIt.hasNext(); ) {
            final SootMethod m = mIt.next();
            debug( "restoring body of "+m );
            Body newBody = Jimple.v().newBody(m);
            Map<Object,Object> newBindings = 
                newBody.importBodyContentsFrom(savedBodies.get(m));
            m.setActiveBody(newBody);
            m.setParameterTypes(savedParameters.get(m));
            ret.putAll( newBindings );
        }
        for( Iterator abcClassIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); abcClassIt.hasNext(); ) {
            final AbcClass abcClass = (AbcClass) abcClassIt.next();
            SootClass cl = abcClass.getSootClass();
            Collection methods = classToMethods.get(cl);
            for( Iterator mIt = new ArrayList(cl.getMethods()).iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if( !methods.contains(m) ) {
                    debug( "removing "+m+" from cl" );
                    cl.removeMethod(m);
                }
            }
            Collection fields = classToFields.get(cl);
            for( Iterator fIt = new ArrayList(cl.getFields()).iterator(); fIt.hasNext(); ) {
                final SootField f = (SootField) fIt.next();
                if( !fields.contains(f) ) {
                    debug( "removing "+f+" from cl" );
                    cl.removeField(f);
                }
            }
            // remove added interfaces
            {
            	Set<SootClass> intfs=new HashSet<SootClass>(cl.getInterfaces());
            	intfs.removeAll(classToInterfaces.get(cl));
            	for (Iterator<SootClass> iIt=intfs.iterator(); iIt.hasNext();) {
            		SootClass intf=iIt.next();
            		debug( "removing "+ intf +" from cl " + cl );
            		cl.removeInterface(intf);
            	}
            }
        }
        // remove added classes
        {
        	Set cls=new HashSet(Scene.v().getApplicationClasses());
        	cls.removeAll(applicationClasses);
        	for (Iterator it=cls.iterator(); it.hasNext();) {
        		SootClass cl=(SootClass)it.next();
        		debug( "removing class "+ cl);
        		Scene.v().removeClass(cl);
        	}
        }
        return ret;
    }

	/**
	 * Retains a class that was added after storing the original state so that it does <i>not</i>
	 * get deleted during restore.
	 * @param c
	 */
	public void retainAddedClass(SootClass c) {
		applicationClasses.add(c);
	}
	
	/**
	 * Returns the interfaces of class c prior to weaving advice.
	 */
	public Collection<SootClass> getOldInterfacesOfClass(SootClass c) {
		if(classToInterfaces.containsKey(c))
			return new HashSet<SootClass>(classToInterfaces.get(c));
		else
			throw new IllegalArgumentException("Class "+c+" not known.");
	}
	
	/**
	 * Returns the set of application classes prior to advice weaving.
	 * (Additional classes may be added during weaving, e.g. for tracematches.)
	 */
	public Collection<SootClass> getOldApplicationClasses() {
		return new HashSet<SootClass>(applicationClasses);
	}
	
	/**
	 * Returns the set of methods of class c before advice weaving.
	 */
	public Collection<SootMethod> getOldMethodsOfClass(SootClass c) {
		if(classToMethods.containsKey(c))
			return new HashSet<SootMethod>(classToMethods.get(c));
		else
			throw new IllegalArgumentException("Class "+c+" not known.");
	}

	/**
	 * Returns the set of fields of class c before advice weaving.
	 */
	public Collection<SootField> getOldFieldsOfClass(SootClass c) {
		if(classToFields.containsKey(c))
			return new HashSet<SootField>(classToFields.get(c));
		else
			throw new IllegalArgumentException("Class "+c+" not known.");
	}

	/**
	 * Returns the parameter list for method m before advice weaving. 
	 */
	public List<Type> getOldParameterListOfMethod(SootMethod m) {
		if(savedParameters.containsKey(m))
			return new LinkedList<Type>(savedParameters.get(m));
		else
			throw new IllegalArgumentException("Method "+m+" not known.");
	}
	
	/**
	 * Returns the body that method m had before advice weaving.
	 * <b>Note that this is not a copy of the body but the original body itself.
	 * Don't tamper with it!</b> 
	 */
	public Body getOldBodyOfMethod(SootMethod m) {
		if(savedBodies.containsKey(m))
			return savedBodies.get(m);
		else
			throw new IllegalArgumentException("Method "+m+" not known.");
	}
}
