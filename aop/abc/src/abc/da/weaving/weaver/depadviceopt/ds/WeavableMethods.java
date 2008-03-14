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
package abc.da.weaving.weaver.depadviceopt.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.EntryPoints;
import soot.MethodOrMethodContext;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;
import abc.main.Main;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;

/**
 * Provides functionality to list all weavable methods, i.e. all methods (which have a body) in all weavable classes or
 * only those that are reachable over a given call graph from any application entry point.
 * 
 * @see EntryPoints#application() 
 * @author Eric Bodden
 */
public class WeavableMethods {
	
	/** set of all weavable methods in the program */
	protected Set<SootMethod> weavableMethods;
	
	/** cache mapping each call graph to its set of reachable weavable methods */
	protected Map<CallGraph,Set<SootMethod>> cgToReachable;
	
	/**
	 * Standard constructor, only executed once on initialization.
	 * Registers all weavable methods.
	 */
	private WeavableMethods() {

		cgToReachable = new IdentityHashMap<CallGraph, Set<SootMethod>>();
		weavableMethods = new HashSet<SootMethod>();
		
		GlobalAspectInfo gai = Main.v().getAbcExtension().getGlobalAspectInfo();
		
		//for each weavable class
		for (Iterator<AbcClass> abcClassIter = gai.getWeavableClasses().iterator(); abcClassIter.hasNext();) {
			AbcClass abcClass = abcClassIter.next();
			SootClass sc = abcClass.getSootClass();
			//for each method			
			for (Iterator<SootMethod> methodIter = sc.methodIterator(); methodIter.hasNext();) {
				SootMethod sm = methodIter.next();
				//if it has a body, register it
				if(sm.hasActiveBody() && MethodCategory.weaveInside(sm)) {
					weavableMethods.add(sm);
				}
			}
		}		
		
		weavableMethods = Collections.unmodifiableSet(weavableMethods);		
	}
	
	/**
	 * Returns the set of all weavable methods.
	 * @return all weavable methods; those are assured to have an active body
	 */
	public Set<SootMethod> getAll() {
		return weavableMethods; 
	}

	/**
	 * Returns all weavable methods reachable from application entry points over edges in cg.
	 * The result is cached per call graph, i.e. the client has to make sure that the call graph passed in
	 * does not change over time. 
	 * @param cg any call graph
	 * @return the set of all reachable weavable methods
	 * @see EntryPoints#application()
	 */
	public Set<SootMethod> getReachable(CallGraph cg) {
		//return cached value if present
		if(cgToReachable.containsKey(cg)) {
			return cgToReachable.get(cg);
		}
		
		//get all reachable methods
		ReachableMethods rm = new ReachableMethods(
				cg,
				new ArrayList<MethodOrMethodContext>(EntryPoints.v().application())
		);
		rm.update();
		
		QueueReader<MethodOrMethodContext> reader = rm.listener();
		Set<SootMethod> reachableWeavableMethods = new HashSet<SootMethod>();
		
		//check for weavable ones
        while(reader.hasNext()) {
            SootMethod method = (SootMethod) reader.next();

            if(weavableMethods.contains(method)) {
            	reachableWeavableMethods.add(method);
            }
        }
        
        //cache
        cgToReachable.put(cg,reachableWeavableMethods);
        
        return reachableWeavableMethods;
	}
	
	//singleton pattern
	
	protected static WeavableMethods instance;
	
	public static WeavableMethods v() {
		if(instance==null) {
			instance = new WeavableMethods();
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
