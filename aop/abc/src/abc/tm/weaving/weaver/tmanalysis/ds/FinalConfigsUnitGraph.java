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
package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.toolkits.callgraph.Units;
import soot.toolkits.graph.DirectedGraph;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.ShadowUtils;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

/**
 * Augments a directed graph over {@link Units} with a graph suffix that models all
 * possible executions following the execution of the associated method, based on
 * interprocedural shadow information.
 * 
 * First we gather the set of all shadows S that share {@link ShadowGroup}s with {@link SymbolShadow}s
 * in this method. 
 * Then we add an infinite loop over a nop-statement annotated with S. This loop is jumped to from all tail units. 
 * 
 * @author Eric Bodden
 */
public class FinalConfigsUnitGraph implements DirectedGraph<Unit> {
    
    protected static Set<Unit> finalUnits = new HashSet<Unit>();
    
    protected final DirectedGraph<Unit> originalGraph;
    protected NopStmt finalUnit;

    public FinalConfigsUnitGraph(DirectedGraph<Unit> originalGraph, SootMethod container, Set<ISymbolShadow> shadowsInMethod, TraceMatch owner) {
        this.originalGraph = originalGraph;
        
        Set<String> overlappingShadowIDs = ShadowUtils.sameShadowGroup(shadowsInMethod);
        Set<ISymbolShadow> overlappingShadows = new HashSet<ISymbolShadow>();
        for (String uniqueId : overlappingShadowIDs) {
            SymbolShadow shadow = SymbolShadow.getSymbolShadowForUniqueID(uniqueId);
            //do not have to take into account shadows that were already disables or shadows that are in the same method
            //(as the latter have already been handled conservatively by the initial assumption)
            if(shadow.isEnabled() && !shadow.getContainer().equals(container)) {
                overlappingShadows.add(shadow);
            }
        }
        
        Map<TraceMatch,Set<ISymbolShadow>> tmToShadows = new HashMap<TraceMatch, Set<ISymbolShadow>>();
        tmToShadows.put(owner, overlappingShadows);
                
        NopStmt nop = Jimple.v().newNopStmt();
        nop.addTag( new SymbolShadowTag(tmToShadows) );
        
        this.finalUnit = nop; 
        finalUnits.add(this.finalUnit);
    }

    /**
     * {@inheritDoc}
     */
    public List<Unit> getHeads() {
        //return heads of original graph
        return originalGraph.getHeads();
    }

    /**
     * {@inheritDoc}
     */
    public List<Unit> getPredsOf(Unit s) {
        if(s==finalUnit) {
            //the final unit has itself as predecessor as well as all tail units
            List<Unit> list = new ArrayList<Unit>();
            list.addAll(originalGraph.getTails());
            list.add(finalUnit);
            return list;
        } else {
            //else delegate
            return originalGraph.getPredsOf(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Unit> getSuccsOf(Unit s) {
        if(originalGraph.getTails().contains(s) || s==finalUnit) {
            //final unit is sucessor of each tail unit and of itself
            return Collections.singletonList((Unit)finalUnit);
        } else {
            //else delegate
            return originalGraph.getSuccsOf(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Unit> getTails() {
        //we have no tails because we loop in the end
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    public Iterator iterator() {
        List<Unit> list = new ArrayList<Unit>();
        //add all units form original graph
        for (Iterator<Unit> iterator = originalGraph.iterator(); iterator.hasNext();) {
            Unit u = iterator.next();
            list.add(u);
        }
        //and final unit
        list.add(finalUnit);
        return list.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return originalGraph.size()+1/*(final unit)*/;
    }
    
    /**
     * @return true if this unit is a synthetic final unit
     */
    public static boolean isASyntheticFinalUnit(Unit u) {
        return finalUnits.contains(u);
    }
    
    /**
     * Resets all data structures.
     */
    public static void reset() {
    	finalUnits.clear();
    }

}
