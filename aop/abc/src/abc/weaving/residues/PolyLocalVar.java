/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.residues;


import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.*;
import polyglot.util.InternalCompilerError;

/** A polymorphic local variable whose type is determined by
 *  the first thing it is set to
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */ 

public class PolyLocalVar extends WeavingVar {
    public String name;
    private Local loc;
    private Type type;

    public WeavingVar inline(ConstructorInliningMap cim) {
        if( loc != null ) throw new InternalCompilerError("can't inline once loc has been set");
        if( type != null ) throw new InternalCompilerError("can't inline once type has been set");
        WeavingVar ret = cim.map(this);
        if(ret == null) {
            ret = new PolyLocalVar(name);
            cim.add(this, ret);
        }
        return ret;
    }
    /** The name parameter is just for debugging purposes;
     *  identity of the variable comes from the reference
     */
    public PolyLocalVar(String name) {
	this.name=name;
    }

    public String toString() {
	return "polylocalvar("+name+":"+type+")";
    }
    
    public void resetForReweaving() { loc=null; }

    public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val) {
	type=val.getType();
	if(loc==null) 
		loc = localgen.generateLocal(type,"pointcutlocal");
	else {
		//Local l=(Local)abc.main.Main.v().getAbcExtension.getWWeaver().getUnitBindings().get(loc);
		//if (l!=null)
		//	loc=l;
	}
	Stmt assignStmt=Jimple.v().newAssignStmt(loc,val);
    if(wc.getKindTag() == null) {
        wc.setKindTag(InstructionKindTag.ADVICE_ARG_SETUP);
    }
    Tagger.tagStmt(assignStmt, wc);
	units.insertAfter(assignStmt,begin);
	return assignStmt;
    }

    public Local get() {
	if(loc==null) 
	    throw new RuntimeException
		("Internal error: someone tried to read from a variable bound "
		 +"to a polymorphic local before it was written");

	Local l=(Local)abc.main.Main.v().getAbcExtension().getWeaver().getUnitBindings().get(loc);
	//if (l!=null)
	//	return l;
	// else
		return loc;
    }

    public boolean hasType() {
	return type!=null;
    }

    public Type getType() {
	if(type==null)
	    throw new RuntimeException
		("Internal error: someone tried to inspect the type of "
		 +"a polymorphic local before it was written");
	return type;
    }


}
