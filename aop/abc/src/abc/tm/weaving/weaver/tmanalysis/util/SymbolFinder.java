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
package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.Local;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger;
import abc.weaving.tagkit.InstructionShadowTag;

/**
 * A forward flow analyis that finds shadows for tracematch symbols.
 * See {@link TMShadowTagger} for more information.
 * @author Eric Bodden
 * @see TMShadowTagger
 */
public class SymbolFinder extends ForwardFlowAnalysis {

	protected HashMap<SootMethod, TraceMatch> syncAdviceMethodToTraceMatch;

	protected HashMap<SootMethod, TraceMatch> someAdviceMethodToTraceMatch;

	protected HashMap<SootMethod, TraceMatch> symbolAdviceMethodToTraceMatch;
	
	protected Set<Stmt> someAdviceMethodCalls;
	
	/**
	 * Constructs a new symbol finder for a given unit graph.
	 * @param graph
	 */
	public SymbolFinder(UnitGraph graph) {
		super(graph);
		
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		
		//initialize helper mappings
		syncAdviceMethodToTraceMatch = new HashMap<SootMethod,TraceMatch>();
		someAdviceMethodToTraceMatch = new HashMap<SootMethod,TraceMatch>();
		symbolAdviceMethodToTraceMatch = new HashMap<SootMethod,TraceMatch>();		
		for (TraceMatch tm : (Collection<TraceMatch>) gai.getTraceMatches()) {
			SootMethod syncMethod = tm.getSynchAdviceMethod();
			syncAdviceMethodToTraceMatch.put(syncMethod, tm);
			SootMethod someMethod = tm.getSomeAdviceMethod();
			someAdviceMethodToTraceMatch.put(someMethod, tm);
			for (String symbol : (Collection<String>)tm.getSymbols()) {
				SootMethod symbolAdviceMethod = tm.getSymbolAdviceMethod(symbol);
				symbolAdviceMethodToTraceMatch.put(symbolAdviceMethod, tm);
			}
		}	
		
		//will be filled in by actual analysis
		someAdviceMethodCalls = new HashSet<Stmt>();
		
		//go!...
		doAnalysis();
	}
	
	/**
	 * @return the someAdviceMethodCalls
	 */
	public Set<Stmt> getSomeAdviceMethodCalls() {
		return new HashSet<Stmt>(someAdviceMethodCalls);
	}
	
	public Map<TraceMatch,Set<ISymbolShadow>> getSymbolsAtSomeAdviceMethodCall(Stmt call) {
		return (Map) getFlowBefore(call);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void flowThrough(Object in, Object d, Object out) {
		Stmt stmt = (Stmt)d;
		HashMap inMap = (HashMap)in;
		HashMap outMap = (HashMap)out;

		outMap.putAll(inMap);
		
		if(stmt.containsInvokeExpr()) {
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			SootMethod targetMethod = invokeExpr.getMethod();
			if(syncAdviceMethodToTraceMatch.containsKey(targetMethod)) {
				//have a sync advice; create new set to accumulate symbols in
				TraceMatch tm = syncAdviceMethodToTraceMatch.get(targetMethod);
				outMap.put(tm, new HashSet());
			} else if(symbolAdviceMethodToTraceMatch.containsKey(targetMethod)) {
				TraceMatch tm = (TraceMatch) symbolAdviceMethodToTraceMatch.get(targetMethod);
				String symbolName = tm.symbolNameForSymbolAdvice(targetMethod);
				List<String> tmVariables = tm.getVariableOrder(symbolName);
				Map <String,Local> varMapping = new HashMap<String,Local>();
				//the symbol method advice should have the same number of arguments as the tm variable
				assert invokeExpr.getArgCount()==tmVariables.size();
				int i=0;
				for (String var : tmVariables) {
					Local local = (Local) invokeExpr.getArg(i++);
					varMapping.put(var, local);
				}
				InstructionShadowTag tag = (InstructionShadowTag) stmt.getTag(InstructionShadowTag.NAME);
				assert tag!=null;
				int shadowId = tag.value();								
				Set<ISymbolShadow> currSymbolSet = (Set<ISymbolShadow>) inMap.get(tm);
				Set<ISymbolShadow> newSet = new HashSet<ISymbolShadow>(currSymbolSet);
                UnitGraph g = (UnitGraph)graph;
				newSet.add(new SymbolShadow(symbolName,varMapping,shadowId,g.getBody().getMethod(),tm));
				outMap.put(tm,newSet);
			} else if(someAdviceMethodToTraceMatch.containsKey(targetMethod)) {
				TraceMatch tm = (TraceMatch) someAdviceMethodToTraceMatch.get(targetMethod);
				outMap.remove(tm);
				someAdviceMethodCalls.add(stmt);
			}
		}
	}
	
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void copy(Object source, Object dest) {
		Map s = (Map) source;
		Map d = (Map) dest;
		d.clear();
		d.putAll(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object entryInitialFlow() {
		return new HashMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void merge(Object in1, Object in2, Object out) {
		Map left = (Map) in1;
		Map right = (Map) in2;
		Map res = (Map) out;
		res.clear();
		res.putAll(left);
		for (Entry rightEntry : (Collection<Entry>)right.entrySet()) {
			if(res.containsKey(rightEntry.getKey())) {
				Set set = (Set) res.get(rightEntry.getKey());
				set.addAll((Set)rightEntry.getValue());
			} else {
				res.put(rightEntry.getKey(), rightEntry.getValue());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object newInitialFlow() {
		return new HashMap();
	}


}
