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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.HashSet;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;

/**
 * For each unit in a given unit graph, computes the set of active shadows that may reach this unit, i.e. may
 * precede this unit in the execution. The before-flow excludes shadows at the current unit, while the after-flow
 * includes them.
 *
 * @author Eric Bodden
 */
public class ReachingActiveShadowsAnalysis extends ForwardFlowAnalysis<Unit,Set<ISymbolShadow>> {

    protected final TraceMatch tm;

    /**
     * @param graph unit graph for a given method
     * @param tm tracematch for which to compute reaching active shadows
     */
    public ReachingActiveShadowsAnalysis(DirectedGraph<Unit> graph, TraceMatch tm) {
        super(graph);
        this.tm = tm;
        doAnalysis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void flowThrough(Set<ISymbolShadow> in, Unit unit, Set<ISymbolShadow> out) {
        out.clear();
        out.addAll(in);
        out.addAll(ShadowUtils.getAllActiveShadows(tm, unit));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copy(Set<ISymbolShadow> source, Set<ISymbolShadow> dest) {
        dest.clear();
        dest.addAll(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<ISymbolShadow> entryInitialFlow() {
        return new HashSet<ISymbolShadow>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void merge(Set<ISymbolShadow> in1, Set<ISymbolShadow> in2, Set<ISymbolShadow> out) {
        out.clear();
        out.addAll(in1);
        out.addAll(in2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<ISymbolShadow> newInitialFlow() {
        return new HashSet<ISymbolShadow>();
    }

   
}
