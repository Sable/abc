/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
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

import polyglot.util.InternalCompilerError;

import abc.weaving.aspectinfo.*;
import abc.tm.weaving.matching.*;

import soot.SootClass;
import soot.SootMethod;

import java.util.*;

/** 
 * Represents a TraceMatch.
 *
 *  @author Julian Tibble
 *  @author Pavel Avgustinov
 */
public class TraceMatch
{
    protected String name;
    protected List formals;
    protected StateMachine state_machine;

    protected Map sym_to_vars;
    protected Map sym_to_ordered_vars;
    protected Map sym_to_advice_name;
    protected Map kind_to_advice_name;

    protected Aspect container;
 
    protected SootClass constraint;

    public TraceMatch(String name, List formals, StateMachine state_machine,
                        Map sym_to_vars, Map sym_to_advice_name,
                        Map kind_to_advice_name, Aspect container)
    {
        this.name = name;
        this.formals = formals;
        this.state_machine = state_machine;
        this.sym_to_vars = sym_to_vars;
        this.sym_to_advice_name = sym_to_advice_name;
        this.kind_to_advice_name = kind_to_advice_name;
        this.container = container;

        this.sym_to_ordered_vars = genOrderedVars();
    }

    protected Map genOrderedVars()
    {
        Map ordered_vars = new HashMap();
        Iterator symbols = sym_to_vars.keySet().iterator();

        // initialise the mapping with an empty list for each symbol
        while (symbols.hasNext()) {
            String symbol = (String) symbols.next();

            if (!ordered_vars.containsKey(symbol))
                ordered_vars.put(symbol, new LinkedList());
        }

        Iterator formals = this.formals.iterator();

        while (formals.hasNext()) {
            Formal f = (Formal) formals.next();
            symbols = sym_to_vars.keySet().iterator();

            while (symbols.hasNext()) {
                String symbol = (String) symbols.next();

                if (((Set) sym_to_vars.get(symbol)).contains(f.getName()))
                    ((List) ordered_vars.get(symbol)).add(f.getName());
            }
        }

        return ordered_vars;
    }

    public SootClass getContainerClass() {
        return container.getInstanceClass().getSootClass();
    }

    public List getFormals() {
        return formals;
    }

    public StateMachine getState_machine() {
        return state_machine;
    }

    public Set getSymbols() {
        return sym_to_vars.keySet();
    }

    public Set getKinds() {
        return kind_to_advice_name.keySet();
    }

    public Map getSym_to_vars() {
        return sym_to_vars;
    }

    public List getVariableOrder(String symbol)
    {
        return (List) sym_to_ordered_vars.get(symbol);
    }

    public String getName() {
        return name;
    }

    public SootMethod getBodyMethod()
    {
        String body_name = getName() + "$body";

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(body_name);

        return sm;
    }

    public SootMethod getSymbolAdviceMethod(String symbol)
    {
        String advice_name = (String) sym_to_advice_name.get(symbol);

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(advice_name);

        return sm;
    }

    public SootMethod getSomeAdviceMethod(String kind)
    {
        String advice_name = (String) kind_to_advice_name.get(kind);

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(advice_name);

        return sm;
    }

    public void setConstraintClass(SootClass constraint)
    {
        this.constraint = constraint;
    }

    public SootClass getConstraintClass()
    {
        return constraint;
    }
}
