/* abc - The AspectBench Compiler
 * Copyright (C) 2009 Eric Bodden
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
 
package abc.da.weaving.weaver.tracing;

import java.io.IOException;
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

import abc.da.fianalysis.PathInfoFinder;
import abc.da.fianalysis.PathInfoFinder.PathInfo;
import abc.da.weaving.aspectinfo.TracePattern;
import abc.main.options.OptionsParser;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.util.Pair;

public class CrossChecker {

	public static void crossCheck() {
		String runtimeTraceFileName = OptionsParser.v().runtime_trace();
		String compileTimeTraceFileName = OptionsParser.v().compile_time_trace();
		if(runtimeTraceFileName==null) {
			throw new RuntimeException("no runtime trace file given; see -runtime-trace-file option!");
		}
		if(compileTimeTraceFileName==null) {
			throw new RuntimeException("no compile-time trace file given; see -compile-time-trace-file option!");
		}
		try {
			List<Event> eventTrace = new RuntimeTraceReader().readTrace(runtimeTraceFileName);
			Pair<Map<Integer,Set<Integer>>,Map<Integer,Set<Integer>>> compileTimeTrace = new CompileTimeTraceReader().readTrace(compileTimeTraceFileName);
			
			
			Set<List<Event>> traces = splitTracesByTracePattern(eventTrace);
			for(List<Event> trace: traces) {
				TracePattern tp = trace.iterator().next().getTracePattern();
				Set<List<Event>> groundTraces = splitByBinding(tp,trace);
				for (List<Event> groundTrace: groundTraces) {
					crossCheck(tp,groundTrace, compileTimeTrace, true);
					crossCheck(tp,groundTrace, compileTimeTrace, false);
				}
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static void crossCheck(TracePattern tp, List<Event> trace, Pair<Map<Integer, Set<Integer>>, Map<Integer, Set<Integer>>> compileTimeTrace, boolean forward) {
		Map<Integer, Set<Integer>> compileTimeStatesOfShadow;
		if(forward) {
			compileTimeStatesOfShadow = compileTimeTrace.fst();
		} else {
			compileTimeStatesOfShadow = compileTimeTrace.snd();
			Collections.reverse(trace);
		}
		
		Set<SMNode> stateSet = new HashSet<SMNode>(tp.getStateMachine().getInitialStates());
		
		List<Event> noLoopTrace = new LinkedList<Event>();
		for(Event e: trace) {
			Set<SMNode> targetStateSet = new HashSet<SMNode>();
			for(SMNode state: stateSet) {
				//perform actual cross-check				
				Set<Integer> states = compileTimeStatesOfShadow.get(e.getShadowId());
				if(states==null||!states.contains(state.getNumber())) {
					String direction = forward ? "FORWARD" : "BACKWARD";
					System.err.println(direction+"-ERROR !!! At shadow "+e.getShadowId()+" land in state "+state+", but this state is not captured at compile time!");
					System.err.println("Shortest path to error: ");
					for(Event ev: noLoopTrace) {
						System.err.println(ev);
					}
				}
				
				for(Iterator<SMEdge> outEdgeIter = state.getOutEdgeIterator();outEdgeIter.hasNext();) {
					SMEdge outEdge = outEdgeIter.next();
					if(!outEdge.isSkipEdge() && outEdge.getLabel().equals(e.getSymbol())) {
						SMNode target = outEdge.getTarget();
						targetStateSet.add(target);
						if(!target.equals(state)) {
							noLoopTrace.add(e);
						}
					}
				}
			}
			if(targetStateSet.isEmpty()) {
				//we are done
				return;
			}
			stateSet = targetStateSet; 
		}
	}


	private static void crossCheckAtShadow(SMNode state, int shadowId) {
		System.err.println(state+"  @  "+shadowId);
	}


	private static Set<List<Event>> splitByBinding(TracePattern tp, List<Event> trace) {
		Map<String, Set<Integer>> variableToAllBindings = new HashMap<String, Set<Integer>>();		
		for (Event event : trace) {
			Map<String, Integer> binding = event.getVariableBinding();
			for(Entry<String,Integer> entry: binding.entrySet()) {
				String var = entry.getKey();
				Integer objectId = entry.getValue();
				Set<Integer> allBindings = variableToAllBindings.get(var);
				if(allBindings==null) {
					allBindings = new HashSet<Integer>();
					variableToAllBindings.put(var, allBindings);
				}
				allBindings.add(objectId);
			}
		}
		
		PathInfoFinder pathInfoFinder = new PathInfoFinder(tp);
		Set<PathInfo> pathInfos = pathInfoFinder.getPathInfos();

		Collection<String> formals = tp.getFormals();
		List<Event> filtered = new LinkedList<Event>(trace);		
		for (String var : formals) {
			Set<Integer> allBindings = variableToAllBindings.get(var);
			for (Integer objectId : allBindings) {
				Set<String> symbolsForObject = new HashSet<String>();
				for(Event e: trace) {
					if(e.canBindTo(var,objectId)) {
						symbolsForObject.add(e.getSymbol());
						if(symbolsForObject.size()==tp.getSymbols().size()) {
							break;
						}
					}
				}
				boolean passesOrphanShadowCheck = false;
				for(PathInfo pathInfo: pathInfos) {
					Set<String> dominatingLabels = new HashSet<String>(pathInfo.getDominatingLabels());
					if(symbolsForObject.containsAll(dominatingLabels)) {
						passesOrphanShadowCheck = true;
					}
				}
				if(!passesOrphanShadowCheck) {
					for (Iterator<Event> iterator = filtered.iterator(); iterator.hasNext();) {
						Event ev =  iterator.next();
						Integer integer = ev.variableToObjectID.get(var);
						if(integer!=null && integer.equals(objectId)) {
							iterator.remove();
						}
					}
				} 
				
			}
		}
		
		
		List<String> certainlyBoundVars = new ArrayList<String>(tp.getFormals());
		for(SMNode finalState : tp.getStateMachine().getFinalStates()) {
			LinkedHashSet<String> boundVars = finalState.boundVars;
			certainlyBoundVars.retainAll(boundVars);
		}
		
		Set<Map<String,Integer>> fullBindings = new HashSet<Map<String,Integer>>();		
		doIt(certainlyBoundVars,0,new HashMap<String,Integer>(),variableToAllBindings, fullBindings);
		
		Set<List<Event>> groundTraces = new HashSet<List<Event>>();
		for (Map<String, Integer> binding : fullBindings) {
			List<Event> groundTrace = new ArrayList<Event>();
			for(Event e: trace) {
				for(Entry<String, Integer> entry: binding.entrySet()) {
					if(e.canBindTo(entry.getKey(), entry.getValue())) {
						groundTrace.add(e);
					}
				}
				
			}
			if(!groundTrace.isEmpty())
				groundTraces.add(groundTrace);
		}
		
		return groundTraces;
	}


	private static void doIt(List<String> certainlyBoundVars, int i, Map<String,Integer> binding, Map<String, Set<Integer>> variableToAllBindings, Set<Map<String, Integer>> fullBindings) {
		if(i==certainlyBoundVars.size()) {
			fullBindings.add(binding);
		} else {
			String var = certainlyBoundVars.get(0);
			Set<Integer> oIDs = variableToAllBindings.get(var);
			for (Integer oID : oIDs) {
				Map<String,Integer> cloned = new HashMap<String, Integer>(binding);
				binding.put(var, oID);
				doIt(certainlyBoundVars,i+1,cloned,variableToAllBindings,fullBindings);
			}
		}
	}

	private static Set<List<Event>> splitTracesByTracePattern(List<Event> eventTrace) {
		Map<TracePattern, List<Event>> tpToTrace = new HashMap<TracePattern, List<Event>>();
		for(Event e: eventTrace) {
			TracePattern tp = e.getTracePattern();
			List<Event> trace = tpToTrace.get(tp);
			if(trace==null) {
				trace = new ArrayList<Event>();
				tpToTrace.put(tp, trace);
			}
			trace.add(e);
		}
		
		return new HashSet<List<Event>>(tpToTrace.values());
	}

}
