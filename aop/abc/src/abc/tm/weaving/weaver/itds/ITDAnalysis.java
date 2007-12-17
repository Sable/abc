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

import abc.tm.weaving.aspectinfo.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.weaver.*;

import abc.weaving.tagkit.*;

import java.util.*;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

public class ITDAnalysis extends AbstractReweavingAnalysis
{
    Map<SootMethod,TraceMatch> methodToTM =
        new HashMap<SootMethod,TraceMatch>();
    Map<SootMethod,String> methodToSymbol =
        new HashMap<SootMethod,String>();

    public boolean analyze()
    {
        TMGlobalAspectInfo gai = (TMGlobalAspectInfo)
            abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();

        populateLookupMaps(gai);

        // For each method in a weavable class, the advice list
        // is retrieved. If the advice list is non-empty the method
        // is analysed.
        for (Object clobj : gai.getWeavableClasses()) {
            AbcClass cl = (AbcClass) clobj;
            for (final SootMethod method : cl.getSootClass().getMethods()) {
                MethodAdviceList mal = gai.getAdviceList(method);
                if (mal != null && !mal.allAdvice().isEmpty())
                    analyzeMethod(method);
            }
        }
        // don't need to weave again -- tm code-generation isn't part of
        // weaving, we'll just update stuff using the results of the analysis
        return false;
    }

    protected void populateLookupMaps(TMGlobalAspectInfo gai)
    {
        for (TraceMatch tm : (Collection<TraceMatch>) gai.getTraceMatches()) {
            for (String symbol : tm.getSymbols())
            {
                SootMethod method = tm.getSymbolAdviceMethod(symbol);
                methodToTM.put(method, tm);
                methodToSymbol.put(method, symbol);
            }
        }
    }

    protected void analyzeMethod(SootMethod method)
    {
        FreshnessAnalysis analysis = new FreshnessAnalysis(
            new ExceptionalUnitGraph(method.getActiveBody()));

        Chain units = method.getActiveBody().getUnits();
        Stmt stmt = (Stmt) units.getFirst();
        while (stmt != null) {
            if (isAdviceCall(stmt))
                analyzeAdviceCall(stmt, analysis);
            stmt = (Stmt) units.getSuccOf(stmt);
        }
    }

    protected void analyzeAdviceCall(Stmt stmt, FreshnessAnalysis analysis)
    {
        InvokeExpr invoke = stmt.getInvokeExpr();
        SootMethod advice = invoke.getMethodRef().resolve();

        // Does this statement call (per-symbol) advice for a tracematch?
        if (methodToTM.containsKey(advice)) {
            // Update the ITD-analysis results for this tracematch and symbol
            TraceMatch tm = methodToTM.get(advice);
            String symbol = methodToSymbol.get(advice);
            List<String> symbol_vars = tm.getVariableOrder(symbol);

            for (int arg = 0; arg < symbol_vars.size(); arg++) {
                String varname = tm.getVariableOrder(symbol).get(arg);
                Set<SootClass> pointsto = null;
                boolean isfresh = analysis.isFresh(stmt, arg);
                if (isfresh)
                    pointsto = analysis.getPointsToSet(stmt, arg);

                tm.getITDAnalysisResults().addShadow(symbol, varname,
                                                     isfresh, pointsto);
            }
        }
    }

    protected boolean isAdviceCall(Stmt stmt)
    {
        String instructionkind = InstructionKindTag.NAME;

        return stmt.hasTag(instructionkind) &&
            stmt.getTag(instructionkind) == InstructionKindTag.ADVICE_EXECUTE;
    }
}
