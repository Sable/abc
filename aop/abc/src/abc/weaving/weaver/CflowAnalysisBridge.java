/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.weaver;
import abc.weaving.matching.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;
import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.paddle.*;
import soot.options.PaddleOptions;

/** Bridge between abc and Soot Paddle cflow analysis.
 *  @author Ondrej Lhotak
 */

public class CflowAnalysisBridge {
    private static void debug(String message) {
        if (abc.main.Debug.v().cflowAnalysis) 
            System.err.println("CFLOW ANALYSIS ***** " + message);
    }   

    static class StackInfo {
        public List/*Shadow*/ shadows = new ArrayList();
        public Map/*Stmt, AdviceApplication*/ stmtMap = new HashMap();
        public AdviceApplication aa( Stmt s ) {
            return (AdviceApplication) stmtMap.get(s); 
        }
    }

    private Map/*CflowSetup, StackInfo*/ stackInfoMap = new HashMap();
    private Map/*Stmt, Load*/ joinPointStmtMap = new HashMap();

    public void run() {

        debug("processing advices");
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator mIt = cl.getSootClass().getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                MethodAdviceList mal = GlobalAspectInfo.v().getAdviceList(m);
                if( mal == null ) continue;
                processAdvices(mal.allAdvice());
            }
        }


        debug("setting up paddle");
        List entryPoints = new ArrayList();
        entryPoints.addAll(EntryPoints.v().implicit());
        entryPoints.addAll(EntryPoints.v().mainsOfApplicationClasses());
        Scene.v().setEntryPoints(entryPoints);
        PhaseOptions.v().setPhaseOption("cg", "enabled:false");
        PhaseOptions.v().setPhaseOption("cg.paddle", "enabled:false");
        PaddleOptions paddleOpts = new soot.options.PaddleOptions(PhaseOptions.v().getPhaseOptions("cg.paddle"));
        PaddleTransformer.v().setup(paddleOpts);


        debug("making join point analysis");
        JoinPointAnalysis jpa = new JoinPointAnalysis( 
            RefType.v("org.aspectj.lang.JoinPoint"),
            Scene.v().getSootClass("org.aspectbench.runtime.reflect.JoinPointImpl") );
        jpa.setup(new HashSet(joinPointStmtMap.keySet()));


        debug("running paddle");
        PaddleTransformer.v().solve(paddleOpts);


        debug("starting cflow analysis");
        debug("skipping cflow analysis");
        if(false){
        BDDCflow cflowAnalysis = new BDDCflow();
        for( Iterator stackIt = stackInfoMap.keySet().iterator(); stackIt.hasNext(); ) {
            final CflowSetup stack = (CflowSetup) stackIt.next();
            debug("analyzing a stack");
            StackInfo si = stackInfo(stack);
            BDDCflowStack bddcfs =
                new BDDCflowStack(cflowAnalysis, si.shadows );
            for( Iterator stmtIt = si.stmtMap.keySet().iterator(); stmtIt.hasNext(); ) {
                final Stmt stmt = (Stmt) stmtIt.next();
                debug("alwaysValid");
                boolean alwaysValid = bddcfs.alwaysValid(stmt);
                debug("alwaysValid: "+alwaysValid);
                debug("neverValid");
                boolean neverValid = bddcfs.neverValid(stmt);
                debug("neverValid: "+neverValid);
                if( alwaysValid || neverValid ) {
                    for( Iterator rbIt = si.aa(stmt).getResidueBoxes().iterator(); rbIt.hasNext(); ) {
                        final ResidueBox rb = (ResidueBox) rbIt.next();
                        if( !(rb.getResidue() instanceof CflowResidue) )
                            continue;
                        CflowResidue cfr = (CflowResidue) rb.getResidue();
                        if( cfr.setup() != stack ) continue;
                        debug("found a residue");
                        if( alwaysValid ) rb.setResidue(AlwaysMatch.v());
                        if( neverValid ) rb.setResidue(NeverMatch.v());
                    }
                }
            }
        }
        }


        debug("getting join point analysis result");
        for( Iterator optimizableIt = jpa.getResult().iterator(); optimizableIt.hasNext(); ) {
            final Stmt optimizable = (Stmt) optimizableIt.next();
            Load load = (Load) joinPointStmtMap.get(optimizable);
            if( load != null ) load.makeStatic();
        }


        debug("done cflow analysis");
    }
    private StackInfo stackInfo( CflowSetup cfs ) {
        StackInfo ret = (StackInfo) stackInfoMap.get(cfs);
        if(ret == null)
            stackInfoMap.put(cfs, ret = new StackInfo());
        return ret;
    }
    private void processAdvices(List/*AdviceApplication*/ adviceList) {
        for( Iterator aaIt = adviceList.iterator(); aaIt.hasNext(); ) {
            final AdviceApplication aa = (AdviceApplication) aaIt.next();
            AbstractAdviceDecl ad = aa.advice;
            if( ad instanceof CflowSetup ) processCflowSetup( aa );
            for( Iterator rbIt = aa.getResidueBoxes().iterator(); rbIt.hasNext(); ) {
                final ResidueBox rb = (ResidueBox) rbIt.next();
                if( rb.getResidue() instanceof CflowResidue) {
                    CflowResidue cfr = (CflowResidue) rb.getResidue();
                    StackInfo si = stackInfo(cfr.setup());
                    si.stmtMap.put(cfr.getIsValidStmt(), aa);
                } else if( rb.getResidue() instanceof Load) {
                    Load load = (Load) rb.getResidue();
                    joinPointStmtMap.put(load.getJoinPointStmt(), load);
                }
            }
        }
    }
    private void processCflowSetup( final AdviceApplication aa ) {
        CflowSetup cfs = (CflowSetup) aa.advice;
        StackInfo si = stackInfo(cfs);
        final ShadowPoints sp = aa.shadowmatch.sp;
        final boolean unconditional = (aa.getResidue() instanceof AlwaysMatch);
        Shadow sh = new Shadow() {
            public SootMethod method() { return aa.shadowmatch.getContainer(); }
            public Stmt pushStmt() { return sp.getBegin(); }
            public Stmt popStmt() { return sp.getEnd(); }
            public boolean unconditional() { return unconditional; } 
        };
        si.shadows.add(sh);
    }
}
