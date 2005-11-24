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
import soot.util.Chain;
import soot.jimple.*;
import polyglot.util.InternalCompilerError;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.*;

/** A residue that sets a local variable to a value
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */ 

public class SetResidue extends Residue {
    
    Local loc;
    Constant val;

    public Residue optimize() { return this; }
    public SetResidue(Local l,Constant v) {
	loc=l;
	val=v;
    }
    public Residue inline(ConstructorInliningMap cim) {
        return new SetResidue(cim.map(loc), (Constant) Jimple.v().cloneIfNecessary(val));
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	// We don't expect the frontend/matcher to produce a residue that does this. 
	// There's no reason we couldn't just do the standard "automatic fail" thing 
	// if there was ever a need, though.
	if(!sense) 
	    throw new InternalCompilerError("SetResidue should never be used negated");


	Stmt assign=Jimple.v().newAssignStmt(loc,val);
    if(wc.getKindTag() == null) {
        wc.setKindTag(InstructionKindTag.ADVICE_ARG_SETUP);
    }
    Tagger.tagStmt(assign, wc);
	units.insertAfter(assign,begin);
	return assign;
    }

    public String toString() {
	return "set("+loc+","+val+")";
    }

}
