/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package abc.tm.weaving.weaver.itds;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.RefLikeType;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.CastExpr;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JCastExpr;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.util.Chain;


/**
 * An intraprocedural freshness analysis that computes for each statement
 * and local, whether or not the local is 'fresh' at that statement - that is,
 * the statement can not be executed twice with the local pointing to the
 * same object.
 *
 * @author Julian Tibble
 */
public class FreshnessAnalysis extends ForwardBranchedFlowAnalysis
{
    /**
     * Map from allocation statements in this method to the index
     * that represents them in flowsets
     */
    protected final HashMap<Stmt,Integer> allocToIndex =
        new HashMap<Stmt,Integer>();

    /**
     * Map from Jimple statements in this method to the index
     * that represents them in flowsets
     */
    protected final IdentityHashMap<Stmt,Integer> stmtToIndex =
        new IdentityHashMap<Stmt,Integer>();

    /**
     * Map from Jimple locals in this method to the index
     * that represents them in flowsets
     */
    protected final HashMap<Local,Integer> localToIndex =
        new HashMap<Local,Integer>();

    protected class AnalysisInfo
    {
        public String toString()
        {
            StringBuffer state = new StringBuffer();
            for (Local local : localToIndex.keySet()) {
                state.append(local.toString());
                state.append(" ");
                if (isFresh(local))
                    state.append("fresh ");
                if (isInternal(local))
                    state.append("internal ");
                state.append(getClassPointsToSet(local).toString());
                state.append("\n");
            }
            return state.toString();
        }

        protected BitSet fresh;
        protected BitSet internal;
        protected BitSet pointsto; 
        protected BitSet predecessors;

        public AnalysisInfo(boolean allinternal)
        {
            int locals = localToIndex.size();
            fresh = new BitSet(locals);
            internal = new BitSet(locals);
            pointsto = new BitSet(locals * allocToIndex.size());
            predecessors = new BitSet(stmtToIndex.size());

            if (allinternal)
                internal.set(0, locals);
        }

        public AnalysisInfo(AnalysisInfo other) {
            this(false);
            fresh.or(other.fresh);
            internal.or(other.internal);
            pointsto.or(other.pointsto);
            predecessors.or(other.predecessors);
        }

        public boolean isFresh(Local local)
        {
            int index = localToIndex.get(local);
            return fresh.get(index);
        }

        public void doAlloc(Local local, Stmt alloc)
        {
            int numallocs = allocToIndex.size();
            int loc = localToIndex.get(local) * numallocs;
            int allocindex = allocToIndex.get(alloc);
            // clear existing pointsto information
            pointsto.set(loc, loc + numallocs - 1, false);
            // set new pointsto information
            pointsto.set(loc + allocindex);
            // remember that the allocated object is internal
            internal.set(localToIndex.get(local));
            // an allocated object is fresh
            fresh.set(localToIndex.get(local));
        }

        public void doAssign(Local destlocal, Local srclocal)
        {
            int numallocs = allocToIndex.size();
            int dest = localToIndex.get(destlocal) * numallocs;
            int src = localToIndex.get(srclocal) * numallocs;

            if (dest == src)
                return;

            for (int i = 0; i < numallocs; i++)
                pointsto.set(dest + i, pointsto.get(src + i));
        }

        public void setExternal(Local local)
        {
            internal.set(localToIndex.get(local), false);
            fresh.set(localToIndex.get(local), false);
        }

        public boolean isInternal(Local local)
        {
            return internal.get(localToIndex.get(local));
        }

        public Set<SootClass> getClassPointsToSet(Local local)
        {
            Set<SootClass> result = new HashSet<SootClass>();
            int numallocs = allocToIndex.size();
            int loc = localToIndex.get(local) * numallocs;

            for (Map.Entry<Stmt,Integer> entry : allocToIndex.entrySet())
            {
                SootClass sc = getAllocatedClass(entry.getKey());
                int index = entry.getValue();
                if (pointsto.get(loc + index))
                    result.add(sc);
            }

            return result;
        }

        public Set<Stmt> getAllocPointsToSet(Local local)
        {
            Set<Stmt> result = new HashSet<Stmt>();
            int numallocs = allocToIndex.size();
            int loc = localToIndex.get(local) * numallocs;

            for (Map.Entry<Stmt,Integer> entry : allocToIndex.entrySet())
            {
                Stmt alloc = entry.getKey();
                int index = entry.getValue();
                if (pointsto.get(loc + index))
                    result.add(alloc);
            }

            return result;
        }

        public void addPredecessor(Stmt stmt)
        {
            int index = stmtToIndex.get(stmt);
            predecessors.set(index);
        }

        public boolean hasPredecessor(Stmt stmt)
        {
            int index = stmtToIndex.get(stmt);
            return predecessors.get(index);
        }

        public void copyTo(AnalysisInfo other)
        {
            other.fresh.clear();        other.fresh.or(fresh);
            other.internal.clear();     other.internal.or(internal);
            other.pointsto.clear();     other.pointsto.or(pointsto);
            other.predecessors.clear(); other.predecessors.or(predecessors);
        }

        public void merge(AnalysisInfo in1, AnalysisInfo in2)
        {
            fresh.clear();
            fresh.or(in1.fresh);
            fresh.and(in2.fresh);

            internal.clear();
            internal.or(in1.internal);
            internal.and(in2.internal);
            
            pointsto.clear();
            pointsto.or(in1.pointsto);
            pointsto.or(in2.pointsto);

            predecessors.clear();
            predecessors.or(in1.predecessors);
            predecessors.or(in2.predecessors);
        }

        public int hashCode()
        {
            return fresh.hashCode() ^ internal.hashCode()
                    ^ pointsto.hashCode() ^ predecessors.hashCode();
        }

        public boolean equals(Object other)
        {
            if (!(other instanceof AnalysisInfo))
                return false;
            AnalysisInfo otherinfo = (AnalysisInfo) other;
            return fresh.equals(otherinfo.fresh) &&
                   internal.equals(otherinfo.internal) &&
                   pointsto.equals(otherinfo.pointsto) &&
                   predecessors.equals(otherinfo.predecessors);
        }
    }

    /**
     * Creates a new analysis for the given graph/
     * @param graph any unit graph
     */
    public FreshnessAnalysis(UnitGraph graph)
    {
        super(graph);
        indexLocals(graph.getBody());
        indexStatementsAndAllocations(graph.getBody());
        doAnalysis();
    }

    /**
     * Initialise the mapping from locals to indices
     */
    protected void indexLocals(Body body)
    {
        int index = 0;
        Chain locals = body.getLocals();
        Local local = (Local) locals.getFirst();

        while (local != null) {
            localToIndex.put(local, index++);
            local = (Local) locals.getSuccOf(local);
        }
    }

    /**
     * Initialise the mappings from statements and classes to indices
     */
    protected void indexStatementsAndAllocations(Body body)
    {
        int stmt_index = 0;
        int alloc_index = 0;
        Chain stmts = body.getUnits();
        Stmt stmt = (Stmt) stmts.getFirst();

        while (stmt != null) {
            stmtToIndex.put(stmt, stmt_index++);
            SootClass sc = getAllocatedClass(stmt);
            if (sc != null)
                allocToIndex.put(stmt, alloc_index++);
            stmt = (Stmt) stmts.getSuccOf(stmt);
        }
    }

    /**
     * Return the class that is allocated by a 'new' statement,
     * or null if the statement is not a 'new' statement.
     */
    protected SootClass getAllocatedClass(Stmt stmt)
    {
        if (!(stmt instanceof AssignStmt))
            return null;
        AssignStmt assign = (AssignStmt) stmt;
        Value val = assign.getRightOp();
        if (!(val instanceof NewExpr))
            return null;
        return ((NewExpr) val).getBaseType().getSootClass();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected void flowThrough(Object flowin, Unit unit,
                               List fallOut, List branchOuts)
    {
        AnalysisInfo in = (AnalysisInfo) flowin;
        AnalysisInfo out = new AnalysisInfo(in);
        Stmt stmt = (Stmt)unit;
		
        if (stmt instanceof AssignStmt) {
            AssignStmt assign = (AssignStmt) stmt;
            handleAssign(assign, out);
        }

        out.addPredecessor(stmt);
		
        // now copy the computed info to all successors
        Iterator i = fallOut.iterator();
        while (i.hasNext())
            copy(out, i.next());
        i = branchOuts.iterator();
        while (i.hasNext())
            copy(out, i.next());
    }
	
    protected void handleAssign(AssignStmt assign, AnalysisInfo out)
    {
        Value lhs = assign.getLeftOp();
        Value rhs = assign.getRightOp();

        if (!(lhs instanceof Local))
            return;

        Local dest = (Local) lhs;

        if (getAllocatedClass(assign) != null) {
            out.doAlloc(dest, assign);
        } else {
            // if there's a cast, find out what is being cast
            if (rhs instanceof CastExpr)
                rhs = ((CastExpr) rhs).getOp();

            if (rhs instanceof Local) {
                Local src = (Local) rhs;
                out.doAssign(dest, src);
            } else {
                out.setExternal(dest);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void copy(Object source, Object dest)
    {
        AnalysisInfo s = (AnalysisInfo) source;
        AnalysisInfo d = (AnalysisInfo) dest;
        s.copyTo(d);
    }

    /**
     * {@inheritDoc}
     */
    protected Object entryInitialFlow()
    {
        return new AnalysisInfo(false);
    }

    /**
     * Treat trap handlers as entries to the method, since the first
     * statements of traps never seem to have any predecessors - why
     * is this? Why am I using an ExceptionalUnitGraph if there are
     * no exceptional edges!?!
     */
    protected boolean treatTrapHandlersAsEntries()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected void merge(Object in1, Object in2, Object out)
    {
        AnalysisInfo outflow = (AnalysisInfo) out;
        outflow.merge((AnalysisInfo) in1, (AnalysisInfo) in2);
    }

    /**
     * {@inheritDoc}
     */
    protected Object newInitialFlow()
    {
        return new AnalysisInfo(true);
    }

    public boolean isFresh(Stmt call, int arg)
    {
        AnalysisInfo ai = (AnalysisInfo) getFlowBefore(call);
        Object argval = call.getInvokeExpr().getArgs().get(arg);

        // if it's a numeric constant or null then it can't be fresh
        if (!(argval instanceof Local))
            return false; 
        Local local = (Local) argval;

        // check individual shadow analysis results
        if (!ai.isInternal(local))
            return false;
        if (!ai.isFresh(local) && ai.hasPredecessor(call))
            return false;

        // check any other shadows that call the same advice
        Chain units = ((UnitGraph) graph).getBody().getUnits();
        Stmt other = (Stmt) units.getFirst();
        while (other != null) {
            if (other.containsInvokeExpr() && call != other &&
                ai.hasPredecessor(other) && !independent(call, other, arg))
                    return false;
            other = (Stmt) units.getSuccOf(other);
        }

        return true;
    }

    protected boolean independent(Stmt call, Stmt other, int arg)
    {
        SootMethod callmeth = call.getInvokeExpr().getMethodRef().resolve();
        SootMethod othermeth = other.getInvokeExpr().getMethodRef().resolve();

        if (callmeth != othermeth)
            return true;
        
        Set<Stmt> pointsto = allocSitesForArg(call, arg);
        pointsto.retainAll(allocSitesForArg(other, arg));
        return pointsto.isEmpty();
    }

    protected Set<Stmt> allocSitesForArg(Stmt call, int arg)
    {
        Local local = (Local) call.getInvokeExpr().getArgs().get(arg);
        AnalysisInfo ai = (AnalysisInfo) getFlowBefore(call);
        return ai.getAllocPointsToSet(local);
    }

    public Set<SootClass> getPointsToSet(Stmt call, int arg)
    {
        Local local = (Local) call.getInvokeExpr().getArgs().get(arg);
        AnalysisInfo ai = (AnalysisInfo) getFlowBefore(call);
        return ai.getClassPointsToSet(local);
    }
}
