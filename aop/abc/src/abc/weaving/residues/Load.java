/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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
import abc.weaving.weaver.WeavingContext;

/** Load a context value into a local or argument,
 *  without boxing or casting
 *  @author Ganesh Sittampalam
 */ 

public class Load extends Residue {
    public ContextValue value;
    public WeavingVar variable;

    public Load(ContextValue value,WeavingVar variable) {
	this.value=value;
	this.variable=variable;
    }


    public void resetForReweaving() {variable.resetForReweaving();}

    
    public String toString() {
	return "load("+value+","+variable+")";
    }
    private boolean isStatic = false;
    /** Set the static flag on this load residue, meaning that the
     * join point info only needs static parts, and therefore can be
     * optimized to not include dynamic parts.
     */
    public void makeStatic() { isStatic = true; }
    private AssignStmt joinPointStmt = null;
    public AssignStmt getJoinPointStmt() { return joinPointStmt; }
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
		begin=jpi.doInit(localgen,units,begin,isStatic);
                if(!isStatic) joinPointStmt = jpi.getMakeJPStmt();
            }

	    return succeed(units,
			   variable.set(localgen,units,begin,wc,value.getSootValue()),
			   fail,
			   sense);
	}

}
