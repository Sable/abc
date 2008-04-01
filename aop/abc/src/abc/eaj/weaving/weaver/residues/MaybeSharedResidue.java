/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

package abc.eaj.weaving.weaver.residues;

import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.thread.ThreadLocalObjectsAnalysis;
import soot.util.Chain;
import abc.eaj.weaving.aspectinfo.MaybeShared;
import abc.eaj.weaving.weaver.maybeshared.TLOAnalysisManager;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.WeavingContext;

/**
 * A residue of the {@link MaybeShared} pointcut.
 * This residue defaults to {@link AlwaysMatch} but can be optimized by calling
 * {@link #optimize()} after {@link ThreadLocalObjectsAnalysis} was performed (via calling
 * {@link TLOAnalysisManager#analyze()}).
 * @author Eric Bodden
 */
public class MaybeSharedResidue extends Residue {

    /** the surrounding method */
	protected final SootMethod container;

    /** the {@link AssignStmt} that was used to access the field */
	private final AssignStmt fieldRefStmt;

	public  MaybeSharedResidue(AssignStmt stmt, SootMethod container) {
		this.fieldRefStmt = stmt;	
		this.container = container;
    }

	/** 
	 * The result of this method depends on when the method is called.
	 * If the {@link TLOAnalysisManager} was not yet applied, this method will return
	 * <code>this</code>, but at the same time register its {@link #fieldSetGetFieldRef} for
	 * subsequent analysis with the {@link TLOAnalysisManager}. 
	 * 
	 * Otherwise, if the {@link TLOAnalysisManager} was already run, this analysis is queried.
	 * If the analysis determines that indeed all accesses are thread-local, then we reduce to
	 * {@link NeverMatch}. Else, we return {@link AlwaysMatch}.  
	 */
	public Residue optimize() {
        if(TLOAnalysisManager.v().analysisPerformed()) {
        	if(TLOAnalysisManager.v().isThreadLocal(fieldRefStmt)) {
        		return NeverMatch.v();
        	} else {
        		return AlwaysMatch.v();
        	}
        } else {
           	TLOAnalysisManager.v().requestAnalysis(fieldRefStmt,container);
        	return this;
        }
    }
    
    public Residue inline(ConstructorInliningMap cim) { return this; }

    public String toString() {
        return "maybeShared("+fieldRefStmt+")";
    }

    /** 
     * Defaults to {@link AlwaysMatch}.
     */
    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {
        return AlwaysMatch.v().codeGen(method, localgen, units, begin, fail, sense, wc);
    }
}
