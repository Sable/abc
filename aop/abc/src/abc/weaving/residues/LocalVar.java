/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.residues;

import soot.Value;
import soot.Local;
import soot.Type;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A variable needed only during residue computation
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */ 

public class LocalVar extends WeavingVar {
    public Type type;
    public String name;
    private Local loc;

    /** The name parameter is just for debugging purposes;
     *  identity of the variable comes from the reference
     */
    public LocalVar(Type type,String name) {
	this.type=type;
	this.name=name;
    }

    public String toString() {
	return "localvar("+name+":"+type+")";
    }

    public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val) {
	if(loc==null) loc = localgen.generateLocal(type,"pointcutlocal");	
	Stmt assignStmt=Jimple.v().newAssignStmt(loc,val);
	units.insertAfter(assignStmt,begin);
	return assignStmt;
    }

    public Local get() {
	if(loc==null) 
	    throw new RuntimeException
		("Internal error: someone tried to read from a variable bound "
		 +"to a pointcut local before it was written");

	return loc;
    }

    public boolean hasType() {
	return true;
    }

    public Type getType() {
	return type;
    }

}
