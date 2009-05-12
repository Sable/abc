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
package abc.da.fsanalysis.flowanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import polyglot.util.ErrorInfo;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.RefLikeType;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.pointer.InstanceKey;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.InverseGraph;
import abc.da.fsanalysis.EnabledShadowSet;
import abc.da.fsanalysis.callgraph.AbstractedCallGraph;
import abc.da.fsanalysis.flowanalysis.ds.Configuration;
import abc.da.fsanalysis.flowanalysis.ds.Disjunct;
import abc.da.fsanalysis.flowanalysis.ds.Configuration.MaxConfigException;
import abc.da.fsanalysis.mustalias.InstanceKeyNonRefLikeType;
import abc.da.fsanalysis.util.SymbolNames;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.aspectinfo.InvertedTracePattern;
import abc.da.weaving.aspectinfo.TracePattern;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Debug;
import abc.main.Main;
import abc.tm.weaving.matching.SMNode;

/**
 * An analysis job is a parameter object to a {@link TMWorklistBasedAnalysis}.
 * It contains all information about a given method, its control flow graph,
 * its shadows and so on. 
 */
public class AnalysisJob {
	
	protected final Set<Shadow> shadowsForMethodAndTracePattern;
	
	protected final TracePattern tm;
	
	protected final Map<Stmt,Set<Shadow>> stmtToShadows;
	
	protected final Map<Local,Stmt> tmLocalsToDefStatements;
	
	protected final DirectedGraph<Unit> unitGraph;
		
	protected final LocalMustAliasAnalysis localMustAliasAnalysis;
	
	protected final LocalMustNotAliasAnalysis localNotMayAliasAnalysis;		
	
	protected final CallGraph abstractedCallGraph;
	
	protected final SootMethod m;
	
	protected final Set<Shadow> overlappingSymbolShadows;

	protected final Map<SootMethod, EnabledShadowSet> methodToTMShadows;
	
	protected final TracePattern inverted;

	protected final DirectedGraph<Unit> invertedUnitGraph;

	protected final Set<Unit> recursiveCallStmts, callStmts;
	
	protected final Map<Stmt,EnabledShadowSet> transitivelyCalledShadows;

	protected final Map<Stmt,Set<SootMethod>> transitivelyCalledMethods;

	protected final Map<Shadow,EnabledShadowSet> shadowToOverlaps;

	/**
	 * Creates a new job.
	 * @param m the method to analyze
	 * @param tm the TracePattern to abstractly interpret over this method
	 * @param shadowsForMethodAndTracePattern the shadows for the given TracePattern in the given method
	 * @param abstractedCallGraph the abstracted call graph (see {@link AbstractedCallGraph})
	 * @param methodToEnabledTMShadows a mapping from each method in the program to all enables shadows in that method
	 */
	public AnalysisJob(SootMethod m, TracePattern tm, Set<Shadow> shadowsForMethodAndTracePattern, CallGraph abstractedCallGraph, Map<SootMethod, EnabledShadowSet> methodToEnabledTMShadows) {
		this.m = m;
		this.tm = tm;
		this.inverted = new InvertedTracePattern(tm);
		this.methodToTMShadows = new HashMap<SootMethod, EnabledShadowSet>(methodToEnabledTMShadows);
		
		this.shadowsForMethodAndTracePattern = new EnabledShadowSet(shadowsForMethodAndTracePattern);		
		
		//initialize stmtToShadows
        Map<Stmt, Set<Shadow>> stmtToShadows = new HashMap<Stmt, Set<Shadow>>();
        for (Shadow shadow : shadowsForMethodAndTracePattern) {
        	if(stmtToShadows.containsKey(shadow.getAdviceBodyInvokeStmt())) {
        		throw new RuntimeException("Multiple shadows invoked at same statement!?");
        	}                	
        	Set<Shadow> singleton = new EnabledShadowSet(Collections.singleton(shadow));
			stmtToShadows.put(shadow.getAdviceBodyInvokeStmt(),singleton);
		}
        //for all other units, store empty set
        for(Unit u: m.getActiveBody().getUnits()) {
        	if(!stmtToShadows.containsKey(u)) {
        		stmtToShadows.put((Stmt) u, Collections.<Shadow>emptySet());
        	}
        }       
        this.stmtToShadows = stmtToShadows;        
        
        //initialize tmLocalsToDefStatements
		Map<Local,Stmt> localToAdviceBodyInvokeStmt = new HashMap<Local, Stmt>();
		for (Shadow shadow : shadowsForMethodAndTracePattern) {
			for (Local l : shadow.getBoundSootLocals()) {
				localToAdviceBodyInvokeStmt.put(l, shadow.getAdviceBodyInvokeStmt());
			}
		}
		this.tmLocalsToDefStatements = Collections.unmodifiableMap(localToAdviceBodyInvokeStmt);
		
		Set<Shadow> overlappingShadows = AdviceDependency.getAllEnabledShadowsOverlappingWith(shadowsForMethodAndTracePattern);		
        Set<SootMethod> symbolAdviceMethods = new LinkedHashSet<SootMethod>();
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
        //remove shadows for this method 
        for (Iterator<Shadow> shadowIter = overlappingShadows.iterator(); shadowIter.hasNext();) {
        	Shadow shadow = shadowIter.next();
        	if(shadow.getContainer().equals(method())){
                shadowIter.remove();
        	}
        }        
        this.overlappingSymbolShadows = new EnabledShadowSet(overlappingShadows);
        
        ExceptionalUnitGraph ug = new ExceptionalUnitGraph(m.getActiveBody());
		
		this.localMustAliasAnalysis = new LocalMustAliasAnalysis(ug,true);
		
		this.localNotMayAliasAnalysis = new LocalMustNotAliasAnalysis(ug);

		this.unitGraph = ug;
		this.invertedUnitGraph = new InverseGraph<Unit>(ug);

		this.abstractedCallGraph = abstractedCallGraph;

		this.transitivelyCalledShadows = new HashMap<Stmt,EnabledShadowSet>();
		this.transitivelyCalledMethods = new HashMap<Stmt,Set<SootMethod>>();
		
		this.callStmts = new LinkedHashSet<Unit>();
		this.recursiveCallStmts = new LinkedHashSet<Unit>();
		for (Unit unit : unitGraph()) {
			Stmt stmt = (Stmt) unit;
			if (stmt.containsInvokeExpr()) {
				Set<Shadow> overlaps = transitivelyCalledOverlappingShadowsFromOtherMethods(stmt);
				if (!overlaps.isEmpty()) {
					callStmts.add(unit);
				}
				if(transitivelyCalledMethods(stmt).contains(method())) {
					callStmts.add(unit);
					recursiveCallStmts.add(stmt);
				}
			}
		}	
		
		Map<Shadow,EnabledShadowSet> shadowToOverlaps = new HashMap<Shadow, EnabledShadowSet>();
		for(Shadow shadow: shadowsForMethodAndTracePattern) {
			EnabledShadowSet overlaps = AdviceDependency.getAllEnabledShadowsOverlappingWith(Collections.singleton(shadow));
			shadowToOverlaps.put(shadow, overlaps);
		}
		
		this.shadowToOverlaps = shadowToOverlaps;
		
		if(Debug.v().debugDA) {
			System.err.println("Number of all call statements: "+callStmts.size());
			System.err.println("Number of potentially-recursive-call statements: "+recursiveCallStmts.size());
			System.err.println("Number of non-recursive call statements: "+(callStmts.size()-recursiveCallStmts.size()));
		}
	}
	
	public TracePattern tracePattern() {
		return tm;
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

	public Set<Shadow> allEnabledTMShadowsInMethod() {
		return Collections.unmodifiableSet(shadowsForMethodAndTracePattern);
	}
	
	public String symbolNameForShadow(Shadow s) {
		return SymbolNames.v().symbolNameForShadow(s);
	}
	
	public SootMethod method() {
		return m;
	}

	public Set<Shadow> enabledShadowsOfStmt(Stmt s) {
		Set<Shadow> set = stmtToShadows.get(s);
		if(set==null)
			throw new IllegalArgumentException("no information for this statement!");
		else {		
			return Collections.unmodifiableSet(set);
		}
	}
	
	private Stmt defStmtOf(Local local) {
		assert tmLocalsToDefStatements.containsKey(local);
		return tmLocalsToDefStatements.get(local);
	}

	public Set<Shadow> enabledOverlappingSymbolShadowsFromOtherMethods() {
		return Collections.unmodifiableSet(overlappingSymbolShadows);
	}
	
	public Set<Shadow> enabledTMShadowsOf(SootMethod m) {
		Set<Shadow> shadows = methodToTMShadows.get(m);
		if(shadows==null) {
			return Collections.emptySet();
		} else {
			//this set is constantly refined
			return Collections.unmodifiableSet(shadows);
		}
	}
	
	public void compute(File traceFile) {
		if(Debug.v().debugDA) {
			System.err.println("Analyzing method: "+method().getSignature());
		}
		
		
		long before = System.currentTimeMillis();
		int numIterations = 1;
		int numShadows = allEnabledTMShadowsInMethod().size();
		try {
			
			while(true) {
				if(Debug.v().debugDA) {
					System.err.println("Iteration number "+(numIterations++)+" of up to "+numShadows+"...");
				}
				Set<Shadow> allShadows = new HashSet<Shadow>(allEnabledTMShadowsInMethod());
				
				//compute for every statement, which states can reach this statement from the program entry
				ReachingStatesAnalysis forwardAnalysis = new ReachingStatesAnalysis(this, true);
				forwardAnalysis.doAnalysis();
				int forwardCounter = Configuration.counter;
				
				//compute for every statement the set of states from which one could reach a final state using the remainder of the program
				ReachingStatesAnalysis backwardAnalysis = new ReachingStatesAnalysis(this, false);
				backwardAnalysis.doAnalysis();
				int backwardCounter = Configuration.counter;
				
				//find unnecessary shadows
				UnnecessaryShadowsAnalysis unnecessaryShadowsAnalysis = new UnnecessaryShadowsAnalysis(this,forwardAnalysis,backwardAnalysis);				
				
				//logging
				if(Debug.v().dumpDA) {
						dumpResults(allShadows, forwardAnalysis, backwardAnalysis, unnecessaryShadowsAnalysis);
				}
				
				//if we found an unnecessary shadow, disable it and re-run the flow-insensitive analysis for this method
				if(unnecessaryShadowsAnalysis.foundUnnnecessaryShadow()) {
					Shadow nopShadow = unnecessaryShadowsAnalysis.getUnnecessaryShadow();
					warn(nopShadow, "nop-shadow");
					nopShadow.disable();
					if(Debug.v().debugDA) {
						System.err.println("Disabling "+symbolNameForShadow(nopShadow)+"-shadow at position:"+nopShadow.getPosition());
						System.err.println("Successfully removed shadow after analysis with "+Math.max(forwardCounter, backwardCounter)+" configurations");
					}
					//quick fi analysis
					AdviceDependency.disableShadowsWithNoStrongSupportByAnyGroup(allEnabledTMShadowsInMethod());
					//if all shadows are disabled now, we can stop
					if(allEnabledTMShadowsInMethod().isEmpty()) {
						break;
					}
				} else {
					//we found no unnecessary shadow; give up
					break;
				}
			}
		} catch(MaxConfigException e) {
			System.err.println(e.getMessage());
			System.err.println("Aborting analysis of: "+method().getSignature());
		}
		if(Debug.v().debugDA) {
			System.err.println("Done analyzing method : "+method().getSignature()+", took: "+(System.currentTimeMillis()-before));
			System.err.println("Done analyzing method with iterations: " +(numIterations-1)+", max was: "+numShadows);
		}				
	}


	private void appendToTraceFile(Set<Shadow> shadowsBefore,
			ReachingStatesAnalysis forwardAnalysis,
			ReachingStatesAnalysis backwardAnalysis, File traceFile) {
		FileOutputStream fos = null;
		PrintWriter out = null;
		try {
			fos = new FileOutputStream(traceFile, true);
			out = new PrintWriter(fos);

			List<String> legendStrings = new ArrayList<String>();			
			for(Shadow shadow: shadowsBefore) {
				legendStrings.add("# "+shadow.getID()+" "+tracePattern().getName()+"."+symbolNameForShadow(shadow)+" @ "+shadow.getPosition());				
			}
			Collections.sort(legendStrings);
			
			for (String string : legendStrings) {
				out.println(string);
			}
			
			for(Shadow shadow: shadowsBefore) {
				Map<String, Set<InstanceKey>> shadowBinding = shadowBindings(shadow);
				Disjunct shadowDisjunct = Disjunct.TRUE.addBindingsForSymbol(shadowBinding);
				
				
				Set<SMNode> statesBefore = new LinkedHashSet<SMNode>();
				{
					ConfigurationSet flowBefore = forwardAnalysis.getFlowBefore(shadow.getAdviceBodyInvokeStmt());
					for (Configuration config : flowBefore.getConfigurations()) {
						if(config.getBinding().isCompatibleTo(shadowDisjunct)) {
							statesBefore.addAll(config.getStates());
						}
					}
				}

				{
					String stateString = "";
					for (Iterator<SMNode> iter = statesBefore.iterator(); iter.hasNext();) {
						SMNode node = iter.next();
						stateString = stateString + node.getNumber();
						if(iter.hasNext()) {
							stateString = stateString + ",";
						}
					}				
					out.println(shadow.getID()+";fw;"+stateString);
				}
				
				Set<SMNode> statesAfter = new LinkedHashSet<SMNode>();
				{
					ConfigurationSet flowAfter = backwardAnalysis.getFlowBefore(shadow.getAdviceBodyInvokeStmt());
					for (Configuration config : flowAfter.getConfigurations()) {
						if(config.getBinding().isCompatibleTo(shadowDisjunct)) {
							statesAfter.addAll(config.getStates());
						}
					}
				}
				
				{
					String stateString = "";
					for (Iterator<SMNode> iter = statesAfter.iterator(); iter.hasNext();) {
						SMNode node = iter.next();
						stateString = stateString + node.getNumber();
						if(iter.hasNext()) {
							stateString = stateString + ",";
						}
					}				
					out.println(shadow.getID()+";bw;"+stateString);
				}
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("Error writing to trace file:");
			e.printStackTrace();
		} finally {
			if(out!=null) {
				out.flush();
				out.close();
			}
			if(fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	private void dumpResults(Set<Shadow> allShadows,
			ReachingStatesAnalysis forwardAnalysis,
			ReachingStatesAnalysis backwardAnalysis, UnnecessaryShadowsAnalysis unnecessaryShadowsAnalysis) {

		System.err.println();
		System.err.println();
		System.err.println(method().getSignature());
		System.err.println();

		for (Unit unit : unitGraph()) {			
			System.err.println();
			
			boolean hasShadow = false;
			for (Shadow shadow : allShadows) {
				if(shadow.getAdviceBodyInvokeStmt().equals(unit)) {
					hasShadow = true;
					break;
				}
			}		
			{
				Set<Configuration> configs = new LinkedHashSet<Configuration>();
				{
					ConfigurationSet forwardFlow = forwardAnalysis.getFlowBefore(unit);
					configs.addAll(forwardFlow.getConfigurations());
				}
				System.err.println("forward:      " + configs);
			}
			{
				Set<Configuration> configs = new LinkedHashSet<Configuration>();
				{
					ConfigurationSet backwardFlow = backwardAnalysis.getFlowAfter(unit);
					configs.addAll(backwardFlow.getConfigurations());
				}
				System.err.println("backward:     " + configs);
			}
			if(hasShadow) {
				System.err.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
			}
			{
				for (Shadow shadow : allShadows) {
					if(shadow.getAdviceBodyInvokeStmt().equals(unit)) {
						String symbol = symbolNameForShadow(shadow);
						System.err.println(symbol+"-shadow at position "+shadow.getPosition());
						System.err.println("states before:     "+unnecessaryShadowsAnalysis.statesBeforeTransition(shadow));
						System.err.print("transitions:       ");
						for(Entry<Integer,Set<Integer>> entry: unnecessaryShadowsAnalysis.transitions(shadow).entrySet()) {
							System.err.print(entry.getKey()+"->"+entry.getValue()+"  ");
						}
						System.err.println();
						System.err.println("live states after: "+unnecessaryShadowsAnalysis.liveStatesSetsAfterTransition(shadow));
						if(unnecessaryShadowsAnalysis.foundUnnnecessaryShadow() && unnecessaryShadowsAnalysis.getUnnecessaryShadow().equals(shadow)) {
							System.err.println("XXXXX THIS SHADOW WAS DEEMED UNNECESSARY! XXXXX");
						}
					}
				}
			}
			if(hasShadow) {
				System.err.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			}
			System.err.println(unit);
			System.err.println();
		}
		System.err.println("==================================================================");
		System.err.println("==================================================================");
		System.err.println("==================================================================");
		System.err.println("==================================================================");
		System.err.println("==================================================================");
	}
	
	/**
	 * @inheritDoc
	 */
	private Map<String, Set<InstanceKey>> reMap(Map<String, Local> bindings) {
        Map<String,Local> origBinding = bindings;
        Map<String,Set<InstanceKey>> newBinding = new HashMap<String, Set<InstanceKey>>();
        for (Map.Entry<String,Local> entry : origBinding.entrySet()) {
            String tmVar = entry.getKey();
            Local adviceLocal = entry.getValue();
            Stmt stmt = defStmtOf(adviceLocal); //may be null, if adviceLocal is not part of this method
            InstanceKey instanceKey = (adviceLocal.getType() instanceof RefLikeType) ?
              new InstanceKey(adviceLocal,stmt,method(),localMustAliasAnalysis(),localNotMayAliasAnalysis()) :
              new InstanceKeyNonRefLikeType(adviceLocal,stmt,method(),localMustAliasAnalysis(),localNotMayAliasAnalysis());
            newBinding.put(tmVar, Collections.singleton(instanceKey));
        }
        return newBinding;
	}

	public TracePattern invertedTracePattern() {
		return inverted;
	}

	public DirectedGraph<Unit> invertedUnitGraph() {
		return invertedUnitGraph;
	}
	
	public Map<String, Set<InstanceKey>> shadowBindings(Shadow shadow) {		
		return reMap(shadow.getAdviceFormalToSootLocal());
		
	}

	protected void warn(Shadow s, String reason) {	
		Main.v().getAbcExtension().forceReportError(ErrorInfo.WARNING, "Shadow was disabled because it is unnecessary ("+reason+"): "+
				tracePattern().getName()+"."+symbolNameForShadow(s), s.getPosition());
	}

	/**
	 * Returns the collection of <code>Shadow</code>s triggered in transitive callees from <code>s</code>.
	 * @param s any statement
	 */
	public Set<Shadow> transitivelyCalledOverlappingShadowsFromOtherMethods(Stmt s) {
		EnabledShadowSet shadows = transitivelyCalledShadows.get(s);
		if(shadows==null) {
			shadows = new EnabledShadowSet();
	        Set<SootMethod> calleeMethods = transitivelyCalledMethods(s);
	
	        // Collect all shadows in calleeMethods
	        for (SootMethod method : calleeMethods) {
	        	Set<Shadow> enabledTMShadowsOf = enabledTMShadowsOf(method);
	        	for (Shadow shadow : enabledTMShadowsOf) {
					shadows.add(shadow);
				}
	        	
	        }
	        
	        //we are only interested in shadows that overlap
	        shadows.retainAll(enabledOverlappingSymbolShadowsFromOtherMethods());
			transitivelyCalledShadows.put(s,shadows);
		}
        return shadows;
	}

	private Set<SootMethod> transitivelyCalledMethods(Stmt s) {
		Set<SootMethod> calledMethods = transitivelyCalledMethods.get(s);
		if(calledMethods==null) {
			calledMethods = new LinkedHashSet<SootMethod>();
			LinkedList<MethodOrMethodContext> methodsToProcess = new LinkedList<MethodOrMethodContext>();
	
			// Collect initial edges out of given statement in methodsToProcess
			Iterator<Edge> initialEdges = abstractedCallGraph().edgesOutOf(s);
			while (initialEdges.hasNext()) {
			    Edge e = initialEdges.next();
			    methodsToProcess.add(e.getTgt());
			    calledMethods.add(e.getTgt().method());
			}
	
			// Collect transitive callees of methodsToProcess
			while (!methodsToProcess.isEmpty()) {
			    MethodOrMethodContext mm = methodsToProcess.removeFirst();
			    Iterator<Edge> mIt = abstractedCallGraph().edgesOutOf(mm);
	
			    while (mIt.hasNext()) {
			        Edge e = mIt.next();
			        if (!calledMethods.contains(e.getTgt().method())) {
			            methodsToProcess.add(e.getTgt());
			            calledMethods.add(e.getTgt().method());
			        }
			    }
			}
			transitivelyCalledMethods.put(s,calledMethods);
		}
		return calledMethods;
	}
	
	public Set<Unit> getCallStmts() {
		return Collections.unmodifiableSet(callStmts);
	}
	
	public Set<Unit> getRecursiveCallStmts() {
		return Collections.unmodifiableSet(recursiveCallStmts);
	}
	
	public Collection<Unit> getHeads() {
		return Collections.unmodifiableCollection(unitGraph.getHeads());
	}
	
	public Collection<Unit> getTails() {
		return Collections.unmodifiableCollection(unitGraph.getTails());
	}
	
	
}
