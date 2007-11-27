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

import abc.weaving.aspectinfo.AbcFactory;
import abc.tm.weaving.aspectinfo.TraceMatch;
import java.util.*;
import soot.SootClass;

public class ITDAnalysisResults
{
    protected TraceMatch tm;

    protected Set<String> initial_symbols;
    protected Set<String> fresh_vars = new HashSet<String>();
    protected Map<String,Set<SootClass>> var_to_itd_targets =
        new HashMap<String,Set<SootClass>>();

    protected boolean tm_applies;
    protected boolean applicable;

    public ITDAnalysisResults(TraceMatch tm)
    {
        this.tm = tm;

        initial_symbols = tm.getInitialSymbols();
        fresh_vars = new HashSet<String>();
        fresh_vars.addAll(tm.getFormalNames());
        tm_applies = false;

        // To apply the itd optimisation, the tracematch must bind
        // some variables, and the initial symbols must bind all the
        // tracematch formals.
        applicable = initialSymbolsBindAllVariables() && !fresh_vars.isEmpty();
    }

    public boolean canOptimise()
    {
        return applicable;
    }

    public void addShadow(String symbol, String varname,
                          boolean fresh, Set<SootClass> pointsto)
    {
        if (!initial_symbols.contains(symbol))
            return;

        tm_applies = true;

        if (!fresh)
            markNonFresh(varname);
        else
            checkAndAddITDTargets(varname, pointsto);
    }

    public Collection<SootClass> itdTargets()
    {
        return var_to_itd_targets.get(itdVariable());
    }

    public String itdVariable()
    {
        if (applicable)
            return fresh_vars.iterator().next();
        throw new RuntimeException("ITD optimisation not applicable");
    }

    protected boolean isWeavable(SootClass sc)
    {
        return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo()
                       .getWeavableClasses().contains(AbcFactory.AbcClass(sc));
    }

    protected void markNonFresh(String varname)
    {
        fresh_vars.remove(varname);
        if (fresh_vars.isEmpty())
            applicable = false;
    }

    protected void checkAndAddITDTargets(String var, Set<SootClass> weave_into)
    {
        for (SootClass sc : weave_into) {
            if (!isWeavable(sc)) {
                markNonFresh(var);
                return;
            }
            if (!var_to_itd_targets.containsKey(var))
                var_to_itd_targets.put(var, new HashSet<SootClass>());

            var_to_itd_targets.get(var).add(sc);
        }
    }

    protected boolean initialSymbolsBindAllVariables()
    {
        for (String symbol : initial_symbols) {
            if (!tm.getVariableOrder(symbol).containsAll(tm.getFormalNames()))
                return false;
        }
        return true;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ITD analysis results for ");
        sb.append(tm.getName());
        sb.append("\n");
        if (!tm_applies) {
            sb.append("  There are no matches for any initial symbols");
        } else if (applicable) {
            sb.append("  Optimisation is applicable\n");
            sb.append("  Instrumenting on TM formal: ");
            sb.append(itdVariable());
            sb.append("\n  The ITDs targets are:\n");
            for (SootClass sc : itdTargets()) {
                sb.append("    ");
                sb.append(sc);
                sb.append("\n");
            }
        } else {
            sb.append("  Optimisation is not applicable");
        }
        return sb.toString();
    }
}
