/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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

import java.util.Iterator;

import soot.Body;
import soot.Local;
import soot.javaToJimple.LocalGenerator;

/**
 * @author Sascha Kuzins
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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

	private int nextLocal=0;
        /**
         *  Generate a local with a given type, using a suggested name.
         *  If that name is already in use, then try using name0, name1 etc
	 *  @param type The type we want
         *  @param name The suggested name
         */
	public Local generateLocal(soot.Type type, String suggestedName){
		//int i=0;
		String name=suggestedName;
		while (bodyContainsLocal(name)) {
			name=suggestedName + (++nextLocal);
		}
		return createLocal(name, type);
	}

	// The following functions are copied from the base class
	 private soot.Local createLocal(String name, soot.Type sootType) {
		 if (sootType instanceof soot.CharType) {
			 sootType = soot.IntType.v();
		 }
		 soot.Local sootLocal = soot.jimple.Jimple.v().newLocal(name, sootType);
		 body.getLocals().add(sootLocal);
		 return sootLocal;
	 }
	private boolean bodyContainsLocal(String name){
		Iterator it = body.getLocals().iterator();
		while (it.hasNext()){
			if (((soot.Local)it.next()).getName().equals(name)) return true;
		}
		return false;
	}
}
