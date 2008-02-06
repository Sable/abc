/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Damien Sereni
 * Copyright (C) 2006 Eric Bodden
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

package abc.eaj.weaving.aspectinfo;

import polyglot.util.Position;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.thread.ThreadLocalObjectsAnalysis;
import abc.eaj.ast.PCMaybeShared;
import abc.eaj.weaving.weaver.residues.MaybeSharedResidue;
import abc.weaving.aspectinfo.ShadowPointcut;
import abc.weaving.matching.GetFieldShadowMatch;
import abc.weaving.matching.SetFieldShadowMatch;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.StmtShadowMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

/**
 * Implementation of the {@link PCMaybeShared} pointcut. This pointcut matches every field access (read or write) which
 * may be shared, i.e. for which a {@link ThreadLocalObjectsAnalysis} cannot determine thread-locality.
 * 
 * The semantics are defined over the woven program, i.e. if an aspect causes an access to become shared,
 * this will soundly be taken into account. On the flipside, the evaluation of this pointcut requires
 * whole-program points-to analysis and {@link ThreadLocalObjectsAnalysis}.
 * 
 * @author Eric Bodden
 */
public class MaybeShared extends ShadowPointcut {

    public MaybeShared(Position pos) {
        super(pos);
    }

    public String toString() {
        return "maybeShared()";
    }

	/** 
	 * Matches any {@link SetFieldShadowMatch} or {@link GetFieldShadowMatch}.
	 * It returns a new {@link MaybeSharedResidue} that holds a link to
	 * the {@link Stmt} that was used for field access and to the surrounding method. 
	 */
	protected Residue matchesAt(ShadowMatch sm) {
        if(sm instanceof SetFieldShadowMatch || sm instanceof GetFieldShadowMatch) {
        	StmtShadowMatch ssm = (StmtShadowMatch) sm;
            return new MaybeSharedResidue((AssignStmt) ssm.getStmt(),ssm.getContainer());
        } else {
        	return NeverMatch.v();
        }
	}
}
