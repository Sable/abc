package abc.weaving.weaver;
import abc.weaving.matching.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;
import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.paddle.*;

/** Bridge between abc and Soot Paddle cflow analysis.
 *  @author Ondrej Lhotak
 */

public class CflowAnalysisBridge {
    static class StackInfo {
        public List/*Shadow*/ shadows = new ArrayList();
        public Map/*Stmt, AdviceApplication*/ stmtMap = new HashMap();
    }

    private Map/*CflowSetup, StackInfo*/ stackInfoMap = new HashMap();

    public void run() {
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator mIt = cl.getSootClass().getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                MethodAdviceList mal = GlobalAspectInfo.v().getAdviceList(m);
                processAdvices(mal.allAdvice());
            }
        }
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
                if( !(rb.getResidue() instanceof CflowResidue) ) continue;
                CflowResidue cfr = (CflowResidue) rb.getResidue();
                StackInfo si = stackInfo(cfr.setup());
                si.stmtMap.put(cfr.getIsValidStmt(), aa);
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
