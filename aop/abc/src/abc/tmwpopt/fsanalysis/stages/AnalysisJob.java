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
package abc.tmwpopt.fsanalysis.stages;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tmwpopt.fsanalysis.SymbolNames;
import abc.tmwpopt.fsanalysis.TMWorklistBasedAnalysis;
import abc.tmwpopt.fsanalysis.callgraph.AbstractedCallGraph;
import abc.tmwpopt.tmtoda.PathInfoFinder;
import abc.tmwpopt.tmtoda.PathInfoFinder.PathInfo;

/**
 * An analysis job is a parameter object to a {@link TMWorklistBasedAnalysis}.
 * It contains all information about a given method, its control flow graph,
 * its shadows and so on. 
 */
public class AnalysisJob {
	
	protected final Set<Shadow> shadowsForMethodAndTracematch;
	
	protected final TraceMatch tm;
	
	protected Map<Stmt,Set<Shadow>> stmtToShadows;
	
	protected final Set<SootMethod> initialShadowAdviceMethods;	
	
	protected final Map<Local,Stmt> tmLocalsToDefStatements;
	
	protected DirectedGraph<Unit> unitGraph;
		
	protected final LocalMustAliasAnalysis localMustAliasAnalysis;
	
	protected final LocalMustNotAliasAnalysis localNotMayAliasAnalysis;		
	
	protected final CallGraph abstractedCallGraph;
	
	protected final int lengthOfLongestPathInfo;
	
	protected final SootMethod m;
	
	protected final Set<Shadow> overlappingSymbolShadows;

	protected final Map<SootMethod, Set<Shadow>> methodToTMShadows;

	/**
	 * Creates a new job.
	 * @param m the method to analyze
	 * @param tm the tracematch to abstractly interpret over this method
	 * @param shadowsForMethodAndTracematch the shadows for the given tracematch in the given method
	 * @param abstractedCallGraph the abstracted call graph (see {@link AbstractedCallGraph})
	 * @param methodToEnabledTMShadows a mapping from each method in the program to all enables shadows in that method
	 */
	public AnalysisJob(SootMethod m, TraceMatch tm, Set<Shadow> shadowsForMethodAndTracematch, CallGraph abstractedCallGraph, Map<SootMethod, Set<Shadow>> methodToEnabledTMShadows) {
		this.m = m;
		this.tm = tm;
		this.methodToTMShadows = methodToEnabledTMShadows;
		
		this.shadowsForMethodAndTracematch = Collections.unmodifiableSet(shadowsForMethodAndTracematch);		
		
		//initialize stmtToShadows
        Map<Stmt, Set<Shadow>> stmtToShadows = new HashMap<Stmt, Set<Shadow>>();
        for (Shadow shadow : shadowsForMethodAndTracematch) {
        	if(stmtToShadows.containsKey(shadow.getAdviceBodyInvokeStmt())) {
        		throw new RuntimeException("Multiple shadows invoked at same statement!?");
        	}                	
        	Set<Shadow> singleton = new HashSet<Shadow>();
        	singleton.add(shadow);
			stmtToShadows.put(shadow.getAdviceBodyInvokeStmt(),singleton);
		}
        this.stmtToShadows = stmtToShadows;        
        
        //initialize initialShadowAdviceMethods
        Set<SootMethod> initialShadowAdviceMethods = new HashSet<SootMethod>();
        for (String initialSym : tm.getInitialSymbols()) {
			SootMethod adviceMethod = tm.getSymbolAdviceMethod(initialSym);
			initialShadowAdviceMethods.add(adviceMethod);
		}
        this.initialShadowAdviceMethods = Collections.unmodifiableSet(initialShadowAdviceMethods);
        
        //initialize tmLocalsToDefStatements
		Map<Local,Stmt> localToAdviceBodyInvokeStmt = new HashMap<Local, Stmt>();
		for (Shadow shadow : shadowsForMethodAndTracematch) {
			for (Local l : shadow.getBoundSootLocals()) {
				localToAdviceBodyInvokeStmt.put(l, shadow.getAdviceBodyInvokeStmt());
			}
		}
		this.tmLocalsToDefStatements = Collections.unmodifiableMap(localToAdviceBodyInvokeStmt);
		
		Set<Shadow> overlappingShadows = AdviceDependency.getAllEnabledShadowsOverlappingWith(shadowsForMethodAndTracematch);		
        overlappingShadows.removeAll(shadowsForMethodAndTracematch);
        Set<SootMethod> symbolAdviceMethods = new HashSet<SootMethod>();
        for (String sym : tm.getSymbols()) {
			SootMethod adviceMethod = tm.getSymbolAdviceMethod(sym);
			symbolAdviceMethods.add(adviceMethod);
		}
        //remove all shadows that do *not* belong to symbol advice, e.g. body/some/sync advice
        for (Iterator<Shadow> iter = overlappingShadows.iterator(); iter.hasNext();) {
			Shadow s = iter.next();
			if(!symbolAdviceMethods.contains(s.getAdviceDecl().getImpl().getSootMethod())) {
				iter.remove();
			}
		}
        
        //remove artificial symbols
        for (Iterator<Shadow> shadowIter = overlappingShadows.iterator(); shadowIter.hasNext();) {
        	Shadow shadow = shadowIter.next();
        	if(symbolNameForShadow(shadow).equals("newDaCapoRun")){
                shadowIter.remove();
        	}
        }        
        this.overlappingSymbolShadows = Collections.unmodifiableSet(overlappingShadows);
        
        ExceptionalUnitGraph ug = new ExceptionalUnitGraph(m.getActiveBody());
		
		this.localMustAliasAnalysis = new LocalMustAliasAnalysis(ug,true);
		
		this.localNotMayAliasAnalysis = new LocalMustNotAliasAnalysis(ug);

		this.unitGraph = ug;
		
		this.abstractedCallGraph = abstractedCallGraph;
		
        int max = 0;
        Set<PathInfo> pathInfos = new PathInfoFinder(tm).getPathInfos();
        for (PathInfo pathInfo : pathInfos) {
			max = Math.max(max, pathInfo.length());
		}
        lengthOfLongestPathInfo = max;        
	}
	
	public TraceMatch traceMatch() {
		return tm;
	}

	public Set<SootMethod> initialShadowAdviceMethods() {
		return initialShadowAdviceMethods;
	}

	public DirectedGraph<Unit> unitGraph() {
		return unitGraph;
	}

	public LocalMustAliasAnalysis localMustAliasAnalysis() {
		return localMustAliasAnalysis;
	}

	public LocalMustNotAliasAnalysis localNotMayAliasAnalysis() {
		return localNotMayAliasAnalysis;
	}

	public CallGraph abstractedCallGraph() {
		return abstractedCallGraph;
	}

	public int lengthOfLongestPathInfo() {
		return lengthOfLongestPathInfo;
	}

	public Set<Shadow> allTMShadowsInMethod() {
		return shadowsForMethodAndTracematch;
	}
	
	public void addStmtAndShadows(Stmt s, Set<Shadow> shadows) {
		if(stmtToShadows.containsKey(s)) {
			throw new RuntimeException("unintentional overwrite");
		}
		stmtToShadows.put(s, shadows);
	}	
	
	public String symbolNameForShadow(Shadow s) {
		return SymbolNames.v().symbolNameForShadow(s);
	}
	
	public SootMethod method() {
		return m;
	}
	
	public StateMachine stateMachine() {
		return tm.getStateMachine();
	}

	public TMStateMachine tmStateMachine() {
		return (TMStateMachine) tm.getStateMachine();
	}
	
	public Set<Shadow> shadowsOfStmt(Stmt s) {
		assert stmtToShadows.containsKey(s);
		Set<Shadow> set = stmtToShadows.get(s);
		if(set==null)
			return Collections.emptySet();
		else
			return Collections.unmodifiableSet(set);
	}
	
	public Stmt defStmtOf(Local local) {
		assert tmLocalsToDefStatements.containsKey(local);
		return tmLocalsToDefStatements.get(local);
	}

	public Set<Shadow> overlappingSymbolShadows() {
		return overlappingSymbolShadows;
	}
	
	public Set<Shadow> possiblyEnabledTMShadowsOf(SootMethod m) {
		Set<Shadow> shadows = methodToTMShadows.get(m);
		if(shadows==null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(shadows);
		}
	}
}
