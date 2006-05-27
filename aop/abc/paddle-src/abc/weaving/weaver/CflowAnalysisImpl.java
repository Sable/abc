/* abc - The AspectBench Compiler
 * Copyright (C) 2004, 2005 Ondrej Lhotak
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.PackManager;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.paddle.BDDCflow;
import soot.jimple.paddle.BDDCflowStack;
import soot.jimple.paddle.PaddleScene;
import soot.tagkit.StringTag;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.AssertResidue;
import abc.weaving.residues.CflowResidue;
import abc.weaving.residues.JoinPointInfo;
import abc.weaving.residues.Load;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.OrResidue;
import abc.weaving.residues.ResidueBox;

/** Bridge between abc and Soot Paddle cflow analysis.
 *  @author Ondrej Lhotak
 *  @author Eric Bodden
 */

public class CflowAnalysisImpl implements ReweavingAnalysis {
    
    private static void debug(String message) {
        if (abc.main.Debug.v().cflowAnalysis) 
            System.err.println("CFLOW ANALYSIS ***** " + message);
    }   
    
    private static void stats(String message) {
        if (abc.main.Debug.v().cflowAnalysisStats) 
            System.err.println("CFLOW STATS ***** " + message);
    }   

    static class StackInfo {
        public StackInfo( boolean bindsArgs ) {
            this.bindsArgs = bindsArgs;
        }
        public boolean bindsArgs;
        public List/*Shadow*/ shadows = new ArrayList();
        public Map/*Stmt, AdviceApplication*/ stmtMap = new HashMap();
        public AdviceApplication aa( Stmt s ) {
            return (AdviceApplication) stmtMap.get(s); 
        }
    }

    interface Shadow extends soot.jimple.paddle.Shadow {
        public AdviceApplication aa();
    }

    private Map/*CflowSetup, StackInfo*/ stackInfoMap = new HashMap();
    private Map/*Local, Load*/ joinPointLocalMap = new HashMap();

    public boolean analyze() {

        debug("processing advices");
        for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator mIt = cl.getSootClass().getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                MethodAdviceList mal = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(m);
                if( mal == null ) continue;
                processAdvices(mal.allAdvice());
            }
        }


        debug("setting up paddle");
        PaddleScene.v().setupJedd();
        /*
        List entryPoints = new ArrayList();
        entryPoints.addAll(EntryPoints.v().implicit());
        entryPoints.addAll(EntryPoints.v().mainsOfApplicationClasses());
        Scene.v().setEntryPoints(entryPoints);
        */
        /*
        PhaseOptions.v().setPhaseOption("cg", "enabled:false");
        PhaseOptions.v().setPhaseOption("cg.paddle", "enabled:false");
        PaddleOptions paddleOpts = new soot.options.PaddleOptions(PhaseOptions.v().getPhaseOptions("cg.paddle"));
        PaddleTransformer.v().setup(paddleOpts);
        */


        /*
        debug("making join point analysis");
        JoinPointAnalysis jpa = new JoinPointAnalysis( 
            RefType.v("org.aspectj.lang.JoinPoint"),
            Scene.v().getSootClass("org.aspectbench.runtime.reflect.JoinPointImpl") );
        jpa.setup(new HashSet(joinPointLocalMap.keySet()));
        */


        debug("running paddle");
        //PaddleTransformer.v().solve(paddleOpts);
        PackManager.v().getPack("cg").apply();

        debug("starting cflow analysis");
        Date startAnalysis = new Date();
        BDDCflow cflowAnalysis = new BDDCflow();
        for( Iterator stackIt = stackInfoMap.keySet().iterator(); stackIt.hasNext(); ) {
            final CflowSetup stack = (CflowSetup) stackIt.next();
            debug("analyzing a stack");
            StackInfo si = stackInfo(stack);
            BDDCflowStack bddcfs =
                new BDDCflowStack(cflowAnalysis, si.shadows, si.stmtMap.keySet(), si.bindsArgs );
            stats("cflowsetup is "+stack);
            stats("update shadows: "+si.shadows.size()+" query shadows: "+si.stmtMap.keySet().size());
            int never = 0;
            for( Iterator stmtIt = bddcfs.neverValid(); stmtIt.hasNext(); ) {
                final Stmt stmt = (Stmt) stmtIt.next();
                never++;
                debug("found never: "+stmt);
                stmt.addTag(new StringTag("never: "+stack));
                for( Iterator rbIt = si.aa(stmt).getResidueBoxes().iterator(); rbIt.hasNext(); ) {
                    final ResidueBox rb = (ResidueBox) rbIt.next();
                    if( !(rb.getResidue() instanceof CflowResidue) ) continue;
                    CflowResidue cfr = (CflowResidue) rb.getResidue();
                    if( cfr.setup() != stack ) continue;
                    debug("found a residue");
                    if( abc.main.Debug.v().checkCflowOpt ) {
                        if( bddcfs.alwaysValid(stmt) ) {
                            rb.setResidue(new AssertResidue("alwaysnever: "
                                        +rb.getResidue().toString()));
                        } else {
                            rb.setResidue(AndResidue.construct(rb.getResidue(),
                                    new AssertResidue("never: "+rb.getResidue().toString())));
                        }
                    } else {
                        rb.setResidue(NeverMatch.v());
                    }
                }
            }
            int always = 0;
            for( Iterator stmtIt = bddcfs.alwaysValid(); stmtIt.hasNext(); ) {
                final Stmt stmt = (Stmt) stmtIt.next();
                always++;
                debug("found always: "+stmt);
                stmt.addTag(new StringTag("always: "+stack));
                for( Iterator rbIt = si.aa(stmt).getResidueBoxes().iterator(); rbIt.hasNext(); ) {
                    final ResidueBox rb = (ResidueBox) rbIt.next();
                    if( !(rb.getResidue() instanceof CflowResidue) ) continue;
                    CflowResidue cfr = (CflowResidue) rb.getResidue();
                    if( cfr.setup() != stack ) continue;
                    debug("found a residue");
                    if( abc.main.Debug.v().checkCflowOpt ) {
                        rb.setResidue(OrResidue.construct(rb.getResidue(),
                                    new AssertResidue("always: "+rb.getResidue().toString())));
                    } else {
                        rb.setResidue(AlwaysMatch.v());
                    }
                }
            }
            if( !abc.main.Debug.v().dontRemovePushPop 
            &&  !abc.main.Debug.v().checkCflowOpt ) {
                int update = 0;
                for( Iterator shIt = bddcfs.unnecessaryShadows(); shIt.hasNext(); ) {
                    final Shadow sh = (Shadow) shIt.next();
                    update++;
                    debug("removing shadow: "+sh);
                    sh.aa().setResidue(NeverMatch.v());
                }
                stats("always: "+always+" never: "+never+" update shadows removed: "+update);
            } else {
                stats("always: "+always+" never: "+never);
            }
            stats(bddcfs.queryStats());
        }
        Date finishAnalysis = new Date();
        stats("Cflow analysis took "+(finishAnalysis.getTime()-startAnalysis.getTime())+" ms");


        /*
        debug("getting join point analysis result");
        for( Iterator optimizableIt = jpa.getResult().iterator(); optimizableIt.hasNext(); ) {
            final Local optimizable = (Local) optimizableIt.next();
            debug( "trying to optimize "+optimizable );
            Load load = (Load) joinPointLocalMap.get(optimizable);
            debug( "load is "+load );
            if( load != null ) load.makeStatic();
        }
        */


        debug("done cflow analysis");
        
        //optimize the residues
        abc.main.Main.v().getAbcExtension().getWeaver().optimizeResidues();
        
        //return whether we want to reweave
        return !abc.main.Debug.v().dontWeaveAfterAnalysis;
    }
    
    private StackInfo stackInfo( CflowSetup cfs ) {
        StackInfo ret = (StackInfo) stackInfoMap.get(cfs);
        if(ret == null)
            stackInfoMap.put(cfs, 
                    ret = new StackInfo(!cfs.getFormals().isEmpty()));
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
                    Stmt s = cfr.getIsValidStmt();
                    debug("found isValidStmt: "+s);
                    if(s != null) si.stmtMap.put(s, aa);
                } else if( rb.getResidue() instanceof Load) {
                    Load load = (Load) rb.getResidue();
                    if(load.value instanceof JoinPointInfo) {
                        joinPointLocalMap.put(load.variable.get(), load);
                    }
                }
            }
        }
    }
    private void processCflowSetup( final AdviceApplication aa ) {
        final CflowSetup cfs = (CflowSetup) aa.advice;
        StackInfo si = stackInfo(cfs);
        final boolean unconditional = (aa.getResidue() instanceof AlwaysMatch);
        Shadow sh = new Shadow() {
            public SootMethod method() { return aa.shadowmatch.getContainer(); }
            public Stmt pushStmt() { return (Stmt) cfs.pushStmts.get(aa); }
            public Stmt popStmt() { return (Stmt) cfs.popStmts.get(aa); }
            public boolean unconditional() { return unconditional; } 
            public AdviceApplication aa() { return aa; }
        };
        si.shadows.add(sh);
    }
    
    /** 
     * {@inheritDoc}
     */
    public void defaultSootArgs(List sootArgs) {
        // The following Soot args need to go at the beginning, so that they
        // may be overridden by explicit command-line options.
        sootArgs.add("-p");
        sootArgs.add("cg");
        sootArgs.add("enabled:true");
        sootArgs.add("-p");
        sootArgs.add("cg.paddle");
        sootArgs.add("enabled:true");
        sootArgs.add("-p");
        sootArgs.add("cg.paddle");
        sootArgs.add("backend:javabdd");
    }

    /** 
     * {@inheritDoc}
     */
    public void enforceSootArgs(List sootArgs) {
        //we override no arguments
    }
    
    /** 
     * {@inheritDoc}
     */
    public void setupWeaving() {
        //nothing to do here
    }
    /** 
     * {@inheritDoc}
     */
    public void tearDownWeaving() {
        //nothing to do here
    }
}
