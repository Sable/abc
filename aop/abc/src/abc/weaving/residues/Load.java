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

import java.util.Vector;
import soot.*;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.*;

/** Load a context value into a local or argument,
 *  without boxing or casting
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */ 

public class Load extends Residue {
    public ContextValue value;
    public WeavingVar variable;

    public Residue optimize() { return this; }
    public Residue inline(ConstructorInliningMap cim) {
        return new Load(value.inline(cim), variable.inline(cim));
    }
    public Load(ContextValue value,WeavingVar variable) {
	this.value=value;
	this.variable=variable;
    }


    public Residue resetForReweaving() {
    	variable.resetForReweaving();
    	return this;
    }

    
    public String toString() {
	return "load("+value+","+variable+")";
    }
    /** Set the static flag on this load residue, meaning that the
     * join point info only needs static parts, and therefore can be
     * optimized to not include dynamic parts.
     */
    public void makeStatic() {
        JoinPointInfo jpi = (JoinPointInfo) value;
        value = new StaticJoinPointInfo(jpi.shadowMatch().getSJPInfo());
    }
	public Stmt codeGen(
		SootMethod method,
		LocalGeneratorEx localgen,
		Chain units,
		Stmt begin,
		Stmt fail,
		boolean sense,
		WeavingContext wc) {

            if(value instanceof JoinPointInfo) {
                JoinPointInfo jpi = (JoinPointInfo) value;
		begin=jpi.doInit(localgen,units,begin);
            }

            RefType jptype = JoinPointInfo.sootType();
            if(value instanceof StaticJoinPointInfo && variable.getType().equals(jptype)) {
                // OL: need to put in a cast
                Local tmp1 = localgen.generateLocal(value.getSootValue().getType(), "jpcast");
                Stmt load = Jimple.v().newAssignStmt(tmp1, value.getSootValue());
                Tagger.tagStmt(load, wc);
                units.insertAfter(load, begin);
                Local tmp2 = localgen.generateLocal(jptype, "jpcast");
                Stmt cast = Jimple.v().newAssignStmt(tmp2, Jimple.v().newCastExpr(tmp1, jptype));
                Tagger.tagStmt(cast, wc);
                units.insertAfter(cast, load);
                return succeed(units,
                            variable.set(localgen,units,cast,wc,tmp2),
                            fail,
                            sense);
            }

	    return succeed(units,
			   variable.set(localgen,units,begin,wc,value.getSootValue()),
			   fail,
			   sense);
	}

}
