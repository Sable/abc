/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Julian Tibble
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

package abc.tm.weaving.aspectinfo;

import abc.tm.weaving.matching.*;
import java.util.*;

public class IndexingScheme
{
    final public static int COLL_MAP   = 0;
    final public static int PRIM_MAP   = 1;
    final public static int WEAK_MAP   = 2;
    final public static int STRONG_MAP = 3;
    final public static int SET        = 4;

    private TraceMatch tm;
    private HashSet structures;
    private IndexStructure[] state_to_structure;


    // public methods

    public IndexingScheme(TraceMatch tm)
    {
        this.tm = tm;
        this.structures = new HashSet();

        TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
        this.state_to_structure = new IndexStructure[sm.getNumberOfStates()];

        if (abc.main.Debug.v().printIndices)
            System.out.println(sm);

        Iterator i = sm.getStateIterator();
        while (i.hasNext()) {
            SMNode state = (SMNode) i.next();
            addState(state);
        }
    }

    public IndexStructure getStructure(SMNode state)
    {
        return state_to_structure[state.getNumber()];
    }

    public int getNumStates()
    {
        return state_to_structure.length;
    }

    public IndexStructure getStructure(int statenum)
    {
        return state_to_structure[statenum];
    }

    public Iterator getIndexingDepths()
    {
        Set depths = new HashSet();
        Iterator i = structuresIterator();

        while (i.hasNext()) {
            IndexStructure structure = (IndexStructure) i.next();
            depths.add(new Integer(structure.height()));
        }

        return depths.iterator();
    }

    public Iterator structuresIterator()
    {
        return structures.iterator();
    }


    // implementation methods

    protected void addState(SMNode state)
    {
        IndexStructure structure;

        if (state.isInitialNode() || state.isFinalNode() || !abc.main.Debug.v().useIndexing) {
            // do not index on any variables
            structure = new IndexStructure();
        } else {
            HashSet indices = new HashSet(state.boundVars);
            if (abc.main.Debug.v().originalIndexChoosing)
                calculateIndicesByIntersection(tm, indices, state);

            HashSet collectable = new HashSet(indices);
            collectable.retainAll(state.collectableWeakRefs);
            HashSet primitive = new HashSet(indices);
            primitive.removeAll(tm.getNonPrimitiveFormalNames());
            HashSet weak = new HashSet(indices);
            weak.retainAll(state.weakRefs);

            indices.removeAll(collectable);
            indices.removeAll(primitive);
            indices.removeAll(weak);

            if (abc.main.Debug.v().printIndices) {
                System.out.println("State " + state.getNumber());
                System.out.println(" - collectable indices: " + collectable);
                System.out.println(" -   primitive indices: " + primitive);
                System.out.println(" -        weak indices: " + weak);
                System.out.println(" -       other indices: " + indices);
            }

            structure =
                new IndexStructure(collectable, primitive, weak, indices);
        }

        addState(state, structure);
    }

    /**
     * calculate indices[i] = intersect[sym] (bound[i] /\ binds[sym])
     *   BUT only for the symbols where the inner
     *       intersection is not empty
     *
     * if some symbols have been annotated as frequent
     * then only consider them when making indexing decisions
     */
    protected void calculateIndicesByIntersection(TraceMatch tm,
                                                HashSet indices,
                                                SMNode cur)
    {
        Collection frequentSymbols = tm.getFrequentSymbols();
        Iterator symIt =
            frequentSymbols == null ? tm.getSymbols().iterator()
                                    : frequentSymbols.iterator();

        while (symIt.hasNext()) {
            String symbol = (String) symIt.next();

            if (frequentSymbols != null && !frequentSymbols.contains(symbol))
                continue;

            HashSet tmp = new HashSet(cur.boundVars);
            tmp.retainAll(tm.getVariableOrder(symbol));

            if (!tmp.isEmpty())
                indices.retainAll(tmp);
        }
    }

    /**
     * Updates the map from index-structures to sets of states,
     * by adding a new state and structure.
     *
     * The map is checked for the existence of the index-structure
     * first, so all states with the same index-structure share the
     * same IndexStructure object.
     */
    protected void addState(SMNode state, IndexStructure structure)
    {
        structures.add(structure);
        state_to_structure[state.getNumber()] = structure;
    }
}
