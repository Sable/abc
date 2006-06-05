/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2006 Eric Bodden
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

package abc.weaving.aspectinfo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.StdErrorQueue;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.util.Chain;
import soot.util.HashChain;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.EmptyFormals;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.ShadowPoints;
import abc.weaving.weaver.WeavingContext;


/** A <code>declare warning</code> or <code>declare error</code> declaration.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Eric Bodden
 */
public class DeclareMessage extends AbstractAdviceDecl {
    public static final int WARNING = 0;
    public static final int ERROR = 1;

    private final String[] sev_name = { "warning", "error" };
    private final int[] polyglot_sev = { ErrorInfo.WARNING, ErrorInfo.SEMANTIC_ERROR };

    private int severity;
    private String message;

    public DeclareMessage(int severity, Pointcut pc, String message, Aspect aspct, Position pos) {
        super(aspct,new MessageAdvice(),pc,new ArrayList(),pos);
        this.severity = severity;
        this.message = message;
    }



    private static void debug(String message) {
        if(abc.main.Debug.v().messageWeaver)
            System.err.println("MSG*** " + message);
    }


    /** Get the severity of the message.
     *  @return either {@link WARNING} or {@link ERROR}.
     */
    public int getSeverity() {
        return severity;
    }

    /** Get the name of the severity of the message.
     *  @return either <code>&qout;warning&quot;</code> or <code>&qout;error&quot;</code>.
     */
    public String getSeverityName() {
        return sev_name[severity];
    }

    /** Get the message to give if the pointcut matches anything. */
    public String getMessage() {
        return message;
    }

    public String toString() {
        return "declare "+sev_name[severity]+": "+pc+": \""+message+"\";";
    }

    public void debugInfo(String prefix,StringBuffer sb) {
        sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
        sb.append(prefix+" pointcut: "+pc+"\n");
        sb.append(prefix+" special: declare "+getSeverityName()+" : "+getMessage());
    }

    public WeavingEnv getWeavingEnv() {
        return new EmptyFormals();
    }

    public WeavingContext makeWeavingContext() {
        return new WeavingContext();
    }

    public static class MessageAdvice implements AdviceSpec {
        public boolean isAfter() { return false; }
        public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
            return AlwaysMatch.v();
        }
        public void weave(SootMethod method,
                          LocalGeneratorEx localgen,
                          AdviceApplication adviceappl) {

            WeavingContext wc=adviceappl.advice.makeWeavingContext();
            ShadowPoints shadowpoints=adviceappl.shadowmatch.sp;
            AbstractAdviceDecl advicedecl=adviceappl.advice;
            Residue residue = adviceappl.getResidue();
            debug("Weaving declare warning at "+shadowpoints.getShadowMatch());
            Body b = method.getActiveBody();
            Chain units = b.getUnits().getNonPatchingChain();
            Stmt beginshadow = shadowpoints.getBegin();
            Stmt followingstmt = (Stmt) units.getSuccOf(beginshadow);
            Stmt failpoint = Jimple.v().newNopStmt();
            units.insertBefore(failpoint,followingstmt);
            debug("Weaving in residue: "+residue);
            residue.codeGen
                (method,localgen,units,beginshadow,failpoint,true,wc);

            debug("Weaving in advice execution statements");
            Chain stmts = advicedecl.makeAdviceExecutionStmts(adviceappl,localgen,wc);
            debug("Generated stmts: " + stmts);
            for( Iterator nextstmtIt = stmts.iterator(); nextstmtIt.hasNext(); ) {
                final Stmt nextstmt = (Stmt) nextstmtIt.next();
                units.insertBefore(nextstmt,failpoint);
            }
        }
    }

    public Residue postResidue(ShadowMatch sm) {
        return AlwaysMatch.v();
    }

    public void generateMessage(ShadowMatch sm) {
        if(abc.main.Main.v()==null) throw new InternalCompilerError("main was null");
        if(abc.main.Main.v().error_queue==null) throw new InternalCompilerError("no error queue");
        abc.main.Main.v().error_queue.enqueue
            (ErrorInfoFactory.newErrorInfo
             (polyglot_sev[severity],
              message,
              sm.getContainer(),
              sm.getHost()));
    }

    public Chain makeAdviceExecutionStmts
        (AdviceApplication aa,LocalGeneratorEx localgen,WeavingContext wc) {
        debug("starting makeAdviceExecutionStmts");
        Chain ret = new HashChain();
        
        ShadowMatch sm = aa.shadowmatch;
        
        debug("making ei");
        ErrorInfo ei = ErrorInfoFactory.newErrorInfo
                    (polyglot_sev[severity],
                    message,
                    sm.getContainer(),
                    sm.getHost());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        debug("making q");
        StdErrorQueue eq = new StdErrorQueue(new PrintStream(baos), 1, "abc");
        debug("displayError");
        eq.displayError(ei);
        debug("calling toString");
        String errorMessage = baos.toString();
        debug("created message");

        Local so = localgen.generateLocal(RefType.v("java.io.PrintStream"),
                "systemout");
        Stmt getSo = Jimple.v().newAssignStmt(so,
                Jimple.v().newStaticFieldRef(Scene.v().makeFieldRef(
                        Scene.v().getSootClass("java.lang.System"),
                        "out",
                        RefType.v("java.io.PrintStream"),
                        true)));
        ret.addLast(getSo);
        debug("created get System.out");

        List stringType = new ArrayList();
        stringType.add(RefType.v("java.lang.String"));
        Stmt print = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(so,
                    Scene.v().makeMethodRef(
                        Scene.v().getSootClass("java.io.PrintStream"),
                        "println",
                        stringType,
                        VoidType.v(),
                        false),
                    StringConstant.v(errorMessage)));
        ret.addLast(print);
        Tagger.tagChain(ret, InstructionKindTag.DECLARE_MESSAGE);
        debug("created println");
        debug("done makeAdviceExecutionStmts");
        return ret;
    }
    /** Report any errors or warnings for this advice application. */
    public void reportMessages(AdviceApplication adviceappl) {
        if(!NeverMatch.neverMatches(adviceappl.getResidue())) {
            ((DeclareMessage) adviceappl.advice).generateMessage(adviceappl.shadowmatch);
        }
    }
}
