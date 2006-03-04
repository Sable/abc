/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
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

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.AdviceWeavingContext;
import abc.weaving.weaver.PointcutCodeGen;
import abc.weaving.weaver.CodeGenException;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.LocalGeneratorEx;

/** A concrete advice declaration.
 *  This is used for advice declared directly in an aspect.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class AdviceDecl extends AbstractAdviceDecl {

    private MethodSig impl;
    private int jp,jpsp,ejp;

    private int nformals; // the number of formals in the advice implementation


    private Map/*<String,Integer>*/ formal_pos_map = new HashMap();
    private Map/*<String,AbcType>*/ formal_type_map = new HashMap();
    private List/*<MethodSig>*/ methods;
    //private List/*<SootMethod>*/ sootMethods;

    public AdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl, Aspect aspct, int jp, int jpsp, int ejp, List methods, Position pos) {

                super(aspct, spec, pc, impl.getFormals(), pos);
                this.impl = impl;
                this.jp = jp;
                this.jpsp = jpsp;
                this.ejp = ejp;
                this.methods = methods;

                int i = 0;
                nformals = impl.getFormals().size();
                Iterator fi = impl.getFormals().iterator();
                while (fi.hasNext()) {
                        Formal f = (Formal) fi.next();
                        formal_pos_map.put(f.getName(), new Integer(i++));
                        formal_type_map.put(f.getName(), f.getType());
                }
        }

    public int getFormalIndex(String name) {
        Integer i = (Integer)formal_pos_map.get(name);
        if (i == null) {
            throw new InternalCompilerError("Advice formal "+name+" not found");
        }
        return i.intValue();
    }

    public AbcType getFormalType(String name) {
        AbcType t = (AbcType)formal_type_map.get(name);
        if(t==null) {
            throw new InternalCompilerError("Advice formal "+name+" not found");
        }
        return t;
    }




    /** Get the signature of the placeholder method that contains the
     *  body of this advice.
     */
    public MethodSig getImpl() {
        return impl;
    }

    public boolean hasJoinPoint() {
        return jp != -1;
    }

    public boolean hasJoinPointStaticPart() {
        return jpsp != -1;
    }

    public boolean hasEnclosingJoinPoint() {
        return ejp != -1;
    }

    public int joinPointPos() {
        return jp;
    }

    public int joinPointStaticPartPos() {
        return jpsp;
    }

    public int enclosingJoinPointPos() {
        return ejp;
    }

    /** return number of formals (useful for determining number of args
     *     for invokes in code generator)
     */
    // Inline and delete?
    public int numFormals() {
         return nformals;
    }

    public Residue preResidue(ShadowMatch sm) {
        return getAspect().getPer().matchesAt(getAspect(),sm);
    }

    public Residue postResidue(ShadowMatch sm) {
        List/*<SootClass>*/ advicethrown
            =getImpl().getSootMethod().getExceptions();

        List/*<SootClass>*/ shadowthrown
            =sm.getExceptions();

        eachadvicethrow:
        for(Iterator advicethrownit=advicethrown.iterator();
            advicethrownit.hasNext();
            ) {
            SootClass advicethrow=(SootClass) (advicethrownit.next());

            // don't care about unchecked exceptions
            if(Scene.v().getOrMakeFastHierarchy().isSubclass
               (advicethrow,Scene.v().getSootClass("java.lang.RuntimeException"))) continue;

            if(Scene.v().getOrMakeFastHierarchy().isSubclass
               (advicethrow,Scene.v().getSootClass("java.lang.Error"))) continue;

            for(Iterator shadowthrownit=shadowthrown.iterator();
                shadowthrownit.hasNext();
                ) {

                SootClass shadowthrow=(SootClass) (shadowthrownit.next());
                if(Scene.v().getOrMakeFastHierarchy().isSubclass(advicethrow,shadowthrow))
                    break eachadvicethrow;
            }

            // FIXME: this should be a multi-position error
            abc.main.Main.v().error_queue.enqueue
                (ErrorInfoFactory.newErrorInfo
                 (ErrorInfo.SEMANTIC_ERROR,
                  "Advice from aspect "
                  +getAspect().getInstanceClass().getSootClass()
                  +" ("+getPosition().file()
                  +", line "+getPosition().line()+")"
                  +" applies here, and throws exception "+advicethrow
                  +" which is not already thrown here",
                  sm.getContainer(),
                  sm.getHost()));

            return NeverMatch.v();

        }

        Residue ret=AlwaysMatch.v();

        // cache the residue in the SJPInfo to avoid multiple field gets?
        // (could do this in the same place we get the JP stuff if we care)

        if(hasJoinPointStaticPart())
            ret=AndResidue.construct
                (ret,new Load
                 (new StaticJoinPointInfo(sm.getSJPInfo()),
                  new AdviceFormal
                  (joinPointStaticPartPos(),
                   RefType.v("org.aspectj.lang.JoinPoint$StaticPart"))));


        if(hasEnclosingJoinPoint())
            ret=AndResidue.construct
                (ret,new Load
                 (new StaticJoinPointInfo(sm.getEnclosing().getSJPInfo()),
                  new AdviceFormal
                  (enclosingJoinPointPos(),
                   RefType.v("org.aspectj.lang.JoinPoint$StaticPart"))));

        if(hasJoinPoint()) {
            ret=AndResidue.construct
                (ret,new Load
                 (new JoinPointInfo(sm),
                  new AdviceFormal
                  (joinPointPos(),
		   abc.weaving.residues.JoinPointInfo.sootType())));
            // make sure the SJP info will be around later for
            // the JoinPointInfo residue
            sm.recordSJPInfo();
        }

        ret=AndResidue.construct
            (ret,getAspect().getPer().getAspectInstance(getAspect(),sm));
        return ret;

    }


    public WeavingContext makeWeavingContext() {

        int nformals = numFormals();
        PointcutCodeGen.debug("There are " + nformals + " formals to the advice method.");
        Vector arglist = new Vector(nformals, 2);
        arglist.setSize(nformals);
        return new AdviceWeavingContext(arglist);
    }



    /** create the invoke to call the advice body */
    public Chain makeAdviceExecutionStmts
        (AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {

        Chain c = new HashChain();

        AdviceWeavingContext awc=(AdviceWeavingContext) wc;

        SootMethod advicemethod = getImpl().getSootMethod();

        for (int i = 0; i < awc.arglist.size(); i++)
            if(awc.arglist.get(i)==null)
                throw new InternalCompilerError
                    ("Formal "+i+" to advice "+advicemethod.getSignature()+" not filled in: "+wc,getPosition());

        Stmt s =Jimple.v().newInvokeStmt
            (Jimple.v().newVirtualInvokeExpr
             (awc.aspectinstance,advicemethod.makeRef(),awc.arglist)
             );
        Tagger.tagStmt(s, InstructionKindTag.ADVICE_EXECUTE);
        Tagger.tagStmt(s, new InstructionSourceTag(adviceappl.advice.sourceId));
        Tagger.tagStmt(s, new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
        if(abc.main.Debug.v().tagResidueCode)
            s.addTag(new soot.tagkit.StringTag
                     ("^^ invocation of advice body - residue: "+adviceappl.getResidue()));
        c.addLast(s);
        return (c);

    }


    public String toString() {
        return "(in aspect "+getAspect().getName()+") "+spec+": "+pc+" >> "+impl+" <<"
            +(hasJoinPoint() ? " thisJoinPoint" : "")
            +(hasJoinPointStaticPart() ? " thisJoinPointStaticPart" : "")
            +(hasEnclosingJoinPoint() ? " thisEnclosingJoinPoint" : "");
    }

    public void debugInfo(String prefix,StringBuffer sb) {
        sb.append(prefix+" in aspect: "+getAspect().getName()+"\n");
        sb.append(prefix+" type: "+spec+"\n");
        sb.append(prefix+" pointcut: "+pc+"\n");
        sb.append(prefix+" implementation: "+impl+"\n");
    }

    public WeavingEnv getWeavingEnv() {
        // FIXME: cache this?
        return new AdviceFormals(this);
    }

    public static int getPrecedence(AdviceDecl a,AdviceDecl b) {
        // We know that we are in the same aspect

        int lexicalfirst,lexicalsecond;

        if(a.getAdviceSpec().isAfter() || b.getAdviceSpec().isAfter()) {
            lexicalfirst=GlobalAspectInfo.PRECEDENCE_SECOND;
            lexicalsecond=GlobalAspectInfo.PRECEDENCE_FIRST;
        } else {
            lexicalfirst=GlobalAspectInfo.PRECEDENCE_FIRST;
            lexicalsecond=GlobalAspectInfo.PRECEDENCE_SECOND;
        }
        
        if(a.getPosition().line() < b.getPosition().line())
            return lexicalfirst;
        if(a.getPosition().line() > b.getPosition().line())
            return lexicalsecond;

        if(a.getPosition().column() < b.getPosition().column())
            return lexicalfirst;
        if(a.getPosition().column() > b.getPosition().column())
            return lexicalsecond;

        // Trying to compare the same advice, I guess... (modulo inlining behaviour)
        return GlobalAspectInfo.PRECEDENCE_NONE;

    }

    public List/*<MethodSig>*/ getLocalMethods() {
        return methods;
    }


    public List/*<SootMethod>*/ getLocalSootMethods() {
        List ret = new ArrayList();
        for (Iterator procs = methods.iterator(); procs.hasNext(); ) {
                MethodSig ms = (MethodSig) procs.next();
                // special treatment for around:
                // ignore the signature because it may have changed
                if (ms.getName().startsWith("around$") ||
                        ms.getName().startsWith("<init>")) { // TODO: fix getSootMethod!! this is not safe!!
                        SootClass sc = ms.getDeclaringClass().getSootClass();
                        SootMethod method=sc.getMethodByName(ms.getName());
                        ret.add(method);
                } else {
                        try {
                                ret.add(ms.getSootMethod());
                        } catch (RuntimeException e) {
                                String msg="Methods of class " + ms.getDeclaringClass().toString() + "\n";
                                SootClass sc = ms.getDeclaringClass().getSootClass();
                                for (Iterator it=sc.getMethods().iterator(); it.hasNext();) {
                                        SootMethod m=(SootMethod)it.next();
                                        msg+=" " + m.toString() + "\n";
                                }
                                throw new RuntimeException(e.getMessage() + "\n" + msg);
                        }
                }

        }
        return ret;
    }


}
