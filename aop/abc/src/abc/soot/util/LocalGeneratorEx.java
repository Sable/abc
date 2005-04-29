/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
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
 * Created on May 14, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.soot.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.javaToJimple.LocalGenerator;

/**
 * @author Sascha Kuzins
 *
 */
public class LocalGeneratorEx extends LocalGenerator {
	
	/**
	 * @param b
	 */
	public LocalGeneratorEx(Body b) {  
		super(b);
		this.body=b;
	}
	private Body body;
	
	public SootMethod getMethod() {
		return body.getMethod();
	}

	private static int id=0;
	
	//private static Map /*Body => ID*/ idMap=new HashMap();
	public static void reset() {
		//idMap.clear();
		id=0;
	}
	/*private static class ID {
		public int getNextID() {
			return id++;
		}
		private int id=0;
	}*/
		
	/*private int getNextID() {
		ID id=(ID)idMap.get(body);
		if (id==null) {
			id=new ID();
			idMap.put(body,id);
		}		
		return id.getNextID();
	}*/
	
        /**
         *  Generate a local with a given type, using a suggested name.
         *  If that name is already in use, then try using name0, name1 etc
	 *  @param type The type we want
         *  @param name The suggested name
         */
	public Local generateLocal(soot.Type type, String suggestedName){
		//int i=0;
		/*String name=suggestedName;
		Set localNames=getLocalNames();
		while (localNames.contains(name)) {
			name=suggestedName + (++nextLocal);
		}*/
		String name=suggestedName + "$" + (id++);
		//if (bodyContainsLocal(name))
		//	throw new RuntimeException("Name already exists: " + name);
		
		return createLocal(name, type);
	}

	public Local generateLocalWithExactName(soot.Type type, String name) {
		return createLocal(name, type);
	}
	// The following functions are copied from the base class
	 private soot.Local createLocal(String name, soot.Type sootType) {
		 soot.Local sootLocal = soot.jimple.Jimple.v().newLocal(name, sootType);
		 body.getLocals().add(sootLocal);
		 return sootLocal;
	 }
	public boolean bodyContainsLocal(String name){
		Iterator it = body.getLocals().iterator();
		while (it.hasNext()){
			if (((soot.Local)it.next()).getName().equals(name)) return true;
		}
		return false;
	}
	public Local getLocalByName(String name){
		Iterator it = body.getLocals().iterator();
		while (it.hasNext()){
			Local l=(soot.Local)it.next();
			if ((l).getName().equals(name)) 
				return l;
		}
		return null;
	}
	
	/*private Set localNames=new HashSet();
	private Set getLocalNames() {
		localNames.clear();
		Iterator it = body.getLocals().iterator();
		while (it.hasNext()){
			localNames.add(((soot.Local)it.next()).getName());
		}
		return localNames;
	}*/
}
