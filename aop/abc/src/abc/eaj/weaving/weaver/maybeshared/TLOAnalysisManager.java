/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.eaj.weaving.weaver.maybeshared;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.PackManager;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.toolkits.thread.IThreadLocalObjectsAnalysis;
import soot.jimple.toolkits.thread.ThreadLocalObjectsAnalysis;
import soot.jimple.toolkits.thread.mhp.UnsynchronizedMhpAnalysis;
import abc.main.Debug;
import abc.main.Main;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.ResidueBox;
import abc.weaving.weaver.AbstractReweavingAnalysis;
import abc.weaving.weaver.Weaver;


public class TLOAnalysisManager extends AbstractReweavingAnalysis {

	protected enum State { INIT, REGISTERING, ANALYZING, DONE };
	
	protected State state = State.INIT;
	
	protected Map<AssignStmt,SootMethod> stmtToOwner = new HashMap<AssignStmt, SootMethod>();
	protected Map<AssignStmt,Boolean> stmtToResult = new HashMap<AssignStmt, Boolean>();
	
	public boolean analyze() {
		boolean success = false;
		
		Weaver weaver = Main.v().getAbcExtension().getWeaver();
		
		state = State.REGISTERING;
		//optimise residues; this will cause MaybeSharedResidue to call back to requestAnalysis(..) and
		//register locals that need analysis in localToOwner 
		weaver.optimizeResidues();		
		state = State.ANALYZING;
		
		if(!stmtToOwner.isEmpty()) {
			
			try{
			
				if(!Scene.v().hasCallGraph() || !Scene.v().hasPointsToAnalysis()) {
					PackManager.v().getPack("cg").apply();
				}
				
				IThreadLocalObjectsAnalysis tloAnalysis =
					new ThreadLocalObjectsAnalysis(new UnsynchronizedMhpAnalysis());
				
				for (Map.Entry<AssignStmt,SootMethod> entry : stmtToOwner.entrySet()) {
					AssignStmt stmtAfterWeaving = entry.getKey();
					AssignStmt stmtBeforeWeaving = (AssignStmt) weaver.reverseRebind(stmtAfterWeaving);
					if(!stmtToResult.containsKey(stmtBeforeWeaving)) {
						SootMethod m = entry.getValue();
						boolean isThreadLocal = tloAnalysis.isObjectThreadLocal(extractFieldRef(stmtAfterWeaving), m);
						if(isThreadLocal) success = true;						
						stmtToResult.put(stmtBeforeWeaving, isThreadLocal);
					}
				}
				
			} finally {

				Set<SootField> localFields = new HashSet<SootField>();
				Set<SootField> sharedFields = new HashSet<SootField>();

				for (Map.Entry<AssignStmt,Boolean> entry : stmtToResult.entrySet()) {
					if(entry.getValue()) {						
						localFields.add(extractFieldRef(entry.getKey()).getField());
					} else {
						sharedFields.add(extractFieldRef(entry.getKey()).getField());
					}
				}
				
				System.err.println("TLO analysis reports "+localFields.size()+" of "+(localFields.size()+sharedFields.size())+" candidate fields as thread-local.");
				System.err.println("Thread-local fields:");
				for (SootField sootField : localFields) {
					System.err.println(sootField);
				}
				
				state = State.DONE;				
				if(success) {
					//force ResidueBox.residueBoxesChanged to be set to "true" so that the residues get optimised 
					new ResidueBox().setResidue(NeverMatch.v());
				}
				
			}
		}
		
		//clean up
		stmtToOwner.clear();
		
		return false;
	}
	
	public void requestAnalysis(AssignStmt fieldRefStmt, SootMethod owner) {
		Weaver weaver = Main.v().getAbcExtension().getWeaver();
		if(state==State.REGISTERING) {
			AssignStmt stmtAfterWeaving = (AssignStmt) weaver.rebind(fieldRefStmt);
			if(!owner.getActiveBody().getUnits().contains(stmtAfterWeaving)) {
				throw new RuntimeException("Stmt is not in current body!");
			}
			stmtToOwner.put(stmtAfterWeaving, owner);
		}
	}
	
	public boolean analysisPerformed() {
		return state == State.DONE;
	}
	
	public boolean isThreadLocal(AssignStmt fieldRefStmt) {
		if(!stmtToResult.containsKey(fieldRefStmt)) {
			throw new RuntimeException("no result for Local "+fieldRefStmt);
		}
		return stmtToResult.get(fieldRefStmt);
	}
	
	protected static FieldRef extractFieldRef(AssignStmt stmt) {
		if(stmt.getRightOp() instanceof FieldRef) {
			return (FieldRef) stmt.getRightOp();
		} else if(stmt.getLeftOp() instanceof FieldRef) {
			return (FieldRef) stmt.getLeftOp();
		} else {
			throw new IllegalArgumentException("Assignment statement holds no field ref!");
		}	
	}
	
	@Override
	public void defaultSootArgs(List<String> sootArgs) {
        super.defaultSootArgs(sootArgs);
    	//enable whole program mode
        sootArgs.add("-w");
        //disable all packs we do not need
        sootArgs.add("-p");
        sootArgs.add("wjtp");
        sootArgs.add("enabled:false");
        sootArgs.add("-p");
        sootArgs.add("wjop");
        sootArgs.add("enabled:false");
        sootArgs.add("-p");
        sootArgs.add("wjap");
        sootArgs.add("enabled:false");
        
    	//enable points-to analysis
        sootArgs.add("-p");
        sootArgs.add("cg");
        sootArgs.add("enabled:true");

        //enable Spark
        sootArgs.add("-p");
        sootArgs.add("cg.spark");
        sootArgs.add("enabled:true");

        sootArgs.add("-p");
        sootArgs.add("jb");
        sootArgs.add("use-original-names:true");
        
        //need to preserve temporary variables in order to have access to them after the initial weaving step 
        Debug.v().cleanupAfterAdviceWeave = false;
	}
	
	@Override
	public void cleanup() {
		reset();
		super.cleanup();
	}
		
	//singleton pattern
	
	protected static TLOAnalysisManager instance;

	private TLOAnalysisManager() {}
	
	public static TLOAnalysisManager v() {
		if(instance==null) {
			instance = new TLOAnalysisManager();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}

}
