/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
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

package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.AspectCodeGen;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.CodeGenException;
import abc.weaving.weaver.*;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.main.Debug;

/** Manufactured advice that is responsible for setting up cflow stacks/counters
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Ondrej Lhotak
 */
public class CflowSetup extends AbstractAdviceDecl {

    public static CflowSetup construct(Aspect aspct,
                                       Pointcut pc,
                                       boolean isBelow,
                                       Hashtable typeMap,
                                       Position pos,
                                       int depth) {
        // this returns a set of String rather than Var because
        // each occurrence will have a different position
        Set/*<String>*/ fvs=new HashSet();
        pc.getFreeVars(fvs);
        List formals=new LinkedList();
        List actuals=new LinkedList();
        Iterator it=fvs.iterator();
        // FIXME make all the formals have type Object
        while(it.hasNext()) {
            String fv=(String) (it.next());
            AbcType type=(AbcType) typeMap.get(fv);
            if(type==null) throw new InternalCompilerError("variable "+fv+" not in typemap");
            Formal f=new Formal(type,fv,pos);
            formals.add(f);
            actuals.add(new Var(fv,pos));
        }

        return new CflowSetup(aspct,pc,isBelow,formals,actuals,pos,depth);
    }

    private boolean isBelow;
    private int depth;

    private List/*<Var>*/ actuals;

    private boolean useCounter;

    private CflowSetup(Aspect aspct,Pointcut pc,boolean isBelow,
                       List/*<Formal>*/ formals,List/*<Var>*/ actuals,
                       Position pos,int depth) {
        super(aspct,new BeforeAfterAdvice(pos),pc,formals,pos);
        this.actuals=actuals;
        this.isBelow=isBelow;
        this.useCounter=
            formals.isEmpty()          // If no free vars, use a counter instead of a stack
            && !abc.main.Debug.v().dontUseCflowCounter;
        this.depth=depth;
    }

    public boolean isBelow() {
        return isBelow;
    }

    public boolean useCounter() {
        return useCounter;
    }

    public int getDepth() {
        return depth;
    }

    public List/*<Var>*/ getActuals() {
        return actuals;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
        sb.append(prefix+" type: "+spec+"\n");
        sb.append(prefix+" pointcut: "+pc+"\n");
        sb.append(prefix+" state: "+(useCounter ? "counter" : "stack")+"\n");
        sb.append(prefix+" special: "+(isBelow ? "cflowbelow" : "cflow")
                  +" setup\n");
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        debugInfo("", sb);
        return sb.toString();
    }

    public static class CflowSetupWeavingContext
        extends WeavingContext
        implements BeforeAfterAdvice.ChoosePhase {

        public Vector/*<Value>*/ bounds;
        public boolean doBefore;
        public void setBefore() { doBefore=true; }
        public void setAfter() { doBefore=false; }

        public CflowSetupWeavingContext(int boundsize) {
            bounds=new Vector(boundsize);
            bounds.setSize(boundsize);
        }
    }

    public static class CflowSetupBound extends WeavingVar {
        private int pos;
        private Type type;
        private boolean mustbox;
        private Local loc=null;
        
        public WeavingVar inline(ConstructorInliningMap cim) {
        	return new CflowSetupBound(pos, type, mustbox);
        }

        public void resetForReweaving() {
            loc=null;
        }
        
        public CflowSetupBound(int pos,Type type,boolean mustbox) {
            this.pos=pos;
            this.type=type;
            this.mustbox=mustbox;
        }

        public String toString() {
            return "cflowbound("+pos+":"+type+")";
        }

        public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,
                        WeavingContext wc,Value val) {
            if(loc==null) loc = localgen.generateLocal(type,"adviceformal");
            Stmt assignStmt=Jimple.v().newAssignStmt(loc,val);
            units.insertAfter(assignStmt,begin);
            ((CflowSetupWeavingContext) wc).bounds.setElementAt(loc,pos);
            return assignStmt;
        }

        public Local get() {
            if(loc==null)
                throw new RuntimeException
                    ("Internal error: someone tried to read from a variable bound "
                     +"to a cflow advice formal before it was written");

            return loc;
        }

        public boolean hasType() {
            return true;
        }

        public Type getType() {
            return type;
        }

        public boolean mustBox() {
            return mustbox;
        }

    }

    // not static deliberately
    public class CflowBoundVars implements WeavingEnv {
        public CflowBoundVars() {
        }

        private Hashtable setupbounds=new Hashtable();

        public WeavingVar getWeavingVar(Var v) {
            if(setupbounds.containsKey(v.getName()))
                return (CflowSetupBound) setupbounds.get(v.getName());

            int index=-1; Type type=null;
            int i=0;
            Iterator it=getFormals().iterator();
            while(it.hasNext()) {
                Formal f=(Formal) (it.next());
                if(f.getName().equals(v.getName())) {
                    index=i;
                    type=f.getType().getSootType();
                }
                i++;
            }
            if(index==-1)
                throw new InternalCompilerError
                    ("Variable "+v.getName()+" not found in context",
                     v.getPosition());

            boolean mustbox;
            if(type instanceof PrimType) {
                type=Restructure.JavaTypeInfo.getBoxingClass(type).getType();
                mustbox=true;
            } else mustbox=false;

            CflowSetupBound setupbound=new CflowSetupBound(index,type,mustbox);

            setupbounds.put(v.getName(),setupbound);
            return setupbound;
        }

        public AbcType getAbcType(Var v) {
            AbcType type=null;
            Iterator it=getFormals().iterator();
            while(it.hasNext()) {
                Formal f=(Formal) (it.next());
                if(f.getName().equals(v.getName()))
                    type=f.getType();
            }
            if(type==null)
                throw new InternalCompilerError
                    ("Variable "+v.getName()+" not found in context",
                     v.getPosition());
            /*
              if(type.getSootType() instanceof PrimType)
              type=new AbcType(Restructure.JavaTypeInfo.getBoxingClass
              (type.getSootType()).getType());
            */
            return type;
        }
    }

    public WeavingEnv getWeavingEnv() {
        return new CflowBoundVars();
    }

    public WeavingContext makeWeavingContext() {
        return new CflowSetupWeavingContext(getFormals().size());
    }

    public void resetForReweaving() {
    	cflowStack=null;
    	cflowCounter=null;
        popStmts=new HashMap();
        pushStmts=new HashMap();
    }
    
    private SootFieldRef cflowStack=null;
    public SootFieldRef getCflowStack() {
        if(cflowStack==null) {

            if (!(cflowCounter==null)) {
                //Why did we ask for a counter AND a stack??
                throw new CodeGenException(
                      "error: counter and stack requested for one cflow");
            }

            SootClass cl=getAspect().getInstanceClass().getSootClass();

            int i=0;
            String name;
            do {
                name="abc$cflowStack$"+i;
                i++;
            } while(cl.declaresFieldByName(name));

            SootClass stackClass=Scene.v()
                .getSootClass("org.aspectbench.runtime.internal.CFlowStack");
            RefType stackType=stackClass.getType();

            SootField cflowStackF=new SootField(name,stackType,
                                                Modifier.PUBLIC | Modifier.STATIC);
            cl.addField(cflowStackF);
            cflowStack=cflowStackF.makeRef();

            SootMethod preClinit=new AspectCodeGen().getPreClinit(cl);
            LocalGeneratorEx lg=new LocalGeneratorEx
                (preClinit.getActiveBody());

            Local loc=lg.generateLocal(stackType,"cflowStack$"+i);
            Chain units=preClinit.getActiveBody().getUnits();

            Stmt returnStmt=(Stmt) units.getFirst();
            while(!(returnStmt instanceof ReturnVoidStmt))
                returnStmt=(Stmt) units.getSuccOf(returnStmt);

            units.insertBefore(Jimple.v().newAssignStmt
                               (loc,Jimple.v().newNewExpr(stackType)),
                               returnStmt);
            units.insertBefore(Jimple.v().newInvokeStmt
                               (Jimple.v().newSpecialInvokeExpr
                                (loc,Scene.v().makeConstructorRef(stackClass,new ArrayList()))),
                               returnStmt);
            units.insertBefore(Jimple.v().newAssignStmt
                               (Jimple.v().newStaticFieldRef(cflowStack),loc),
                               returnStmt);
        }
        return cflowStack;
    }

    // getCflowCounter retrieves the counter associated with a pcd
    //  - didn't use getCflowStack for this b/c confusing


    private SootFieldRef cflowCounter=null;
    public SootFieldRef getCflowCounter() {
        if(cflowCounter==null) {

            if (!(cflowStack==null)) {
                //Why did we ask for a counter AND a stack??
                throw new CodeGenException(
                      "error: counter and stack requested for one cflow");
            }

            SootClass cl=getAspect().getInstanceClass().getSootClass();

            int i=0;
            String name;
            do {
                name="abc$cflowCounter$"+i;
                i++;
            } while(cl.declaresFieldByName(name));

            SootClass counterClass=Scene.v()
                .getSootClass("org.aspectbench.runtime.internal.CFlowCounter");
            RefType counterType=counterClass.getType();

            SootField cflowCounterF=new SootField(name,counterType,
                                                  Modifier.PUBLIC | Modifier.STATIC);
            cl.addField(cflowCounterF);

            cflowCounter=cflowCounterF.makeRef();

            SootMethod preClinit=new AspectCodeGen().getPreClinit(cl);
            LocalGeneratorEx lg=new LocalGeneratorEx
                (preClinit.getActiveBody());

            Local loc=lg.generateLocal(counterType,"cflowCounter$"+i);
            Chain units=preClinit.getActiveBody().getUnits();

            Stmt returnStmt=(Stmt) units.getFirst();
            while(!(returnStmt instanceof ReturnVoidStmt))
                returnStmt=(Stmt) units.getSuccOf(returnStmt);

            units.insertBefore(Jimple.v().newAssignStmt
                               (loc,Jimple.v().newNewExpr(counterType)),
                               returnStmt);
            units.insertBefore(Jimple.v().newInvokeStmt
                               (Jimple.v().newSpecialInvokeExpr
                                (loc,Scene.v().makeConstructorRef(counterClass,new ArrayList()))),
                               returnStmt);
            units.insertBefore(Jimple.v().newAssignStmt
                               (Jimple.v().newStaticFieldRef(cflowCounter),loc),
                               returnStmt);
        }
        return cflowCounter;
    }

    public Map/*AdviceApplication->Stmt*/ popStmts = new HashMap();
    public Map/*AdviceApplication->Stmt*/ pushStmts = new HashMap();

    public Chain makeAdviceExecutionStmts
         (AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {

        CflowSetupWeavingContext cswc=(CflowSetupWeavingContext) wc;

        if (useCounter) {

            Chain c=new HashChain();
            SootClass counterClass=Scene.v()
                .getSootClass("org.aspectbench.runtime.internal.CFlowCounter");
            Local cflowCounter=localgen.generateLocal(counterClass.getType(),"cflowcounter");

            if (cswc.doBefore) {

                // call cflowcounter.inc()

            SootMethodRef inc=Scene.v().makeMethodRef(counterClass,"inc",new ArrayList(),VoidType.v(),false);
            c.addLast(Jimple.v().newAssignStmt
                      (cflowCounter,Jimple.v().newStaticFieldRef(getCflowCounter())));
            Stmt pushStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(cflowCounter,inc));
            c.addLast(pushStmt);
            pushStmts.put(adviceappl, pushStmt);
            return c;

            } else {

                // call cflowcounter.dec()

            SootMethodRef dec=Scene.v().makeMethodRef(counterClass,"dec",new ArrayList(),VoidType.v(),false);
            c.addLast(Jimple.v().newAssignStmt
                      (cflowCounter,Jimple.v().newStaticFieldRef(getCflowCounter())));
            Stmt popStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(cflowCounter,dec));
            c.addLast(popStmt);
            popStmts.put(adviceappl, popStmt);
            return c;

            }

        } else {

        if(cswc.doBefore) {

            Chain c = new HashChain();
            Type object=Scene.v().getSootClass("java.lang.Object").getType();

            Local l=localgen.generateLocal
                (ArrayType.v(object,1),"cflowbounds");

            c.addLast
                (Jimple.v().newAssignStmt
                 (l,Jimple.v().newNewArrayExpr(object,IntConstant.v(getFormals().size()))));

            for(int i=0;i<getFormals().size();i++)
                c.addLast(Jimple.v().newAssignStmt
                          (Jimple.v().newArrayRef(l,IntConstant.v(i)),
                           (Value) (cswc.bounds.get(i))));


            SootClass stackClass=Scene.v()
                .getSootClass("org.aspectbench.runtime.internal.CFlowStack");

            ArrayList types=new ArrayList(1);
            types.add(ArrayType.v(object,1));
            SootMethodRef push=Scene.v().makeMethodRef(stackClass,"push",types,VoidType.v(),false);

            Local cflowStack=localgen.generateLocal(stackClass.getType(),"cflowstack");
            c.addLast(Jimple.v().newAssignStmt
                      (cflowStack,Jimple.v().newStaticFieldRef(getCflowStack())));

            Stmt pushStmt = Jimple.v().newInvokeStmt
                      (Jimple.v().newVirtualInvokeExpr(cflowStack,push,l));
            c.addLast(pushStmt);
            pushStmts.put(adviceappl, pushStmt);

            return c;
        } else {
            Chain c=new HashChain();
            SootClass stackClass=Scene.v()
                .getSootClass("org.aspectbench.runtime.internal.CFlowStack");
            SootMethodRef pop=Scene.v().makeMethodRef(stackClass,"pop",new ArrayList(),VoidType.v(),false);
            Local cflowStack=localgen.generateLocal(stackClass.getType(),"cflowstack");
            c.addLast(Jimple.v().newAssignStmt
                      (cflowStack,Jimple.v().newStaticFieldRef(getCflowStack())));
            Stmt popStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(cflowStack,pop));
            c.addLast(popStmt);
            popStmts.put(adviceappl, popStmt);
            return c;
        }

        }

    }

    public static int getPrecedence(CflowSetup a,CflowSetup b) {
        // We know that the belows are the same

        if(a.isBelow()) {
            if(a.getDepth() < b.getDepth()) return GlobalAspectInfo.PRECEDENCE_FIRST;
            if(a.getDepth() > b.getDepth()) return GlobalAspectInfo.PRECEDENCE_SECOND;
        } else {
            if(a.getDepth() > b.getDepth()) return GlobalAspectInfo.PRECEDENCE_FIRST;
            if(a.getDepth() < b.getDepth()) return GlobalAspectInfo.PRECEDENCE_SECOND;
        }

        if(!a.getDefiningAspect().getName().equals(b.getDefiningAspect().getName()))
            return GlobalAspectInfo.v().getPrecedence
                (a.getDefiningAspect(),b.getDefiningAspect());

        // FIXME: Best guess is to compare by positions, but is this correct w.r.t inlining?
        if(a.getPosition().line() < b.getPosition().line())
            return GlobalAspectInfo.PRECEDENCE_FIRST;
        if(a.getPosition().line() > b.getPosition().line())
            return GlobalAspectInfo.PRECEDENCE_SECOND;

        if(a.getPosition().column() < b.getPosition().column())
            return GlobalAspectInfo.PRECEDENCE_FIRST;
        if(a.getPosition().column() > b.getPosition().column())
            return GlobalAspectInfo.PRECEDENCE_SECOND;

        // Trying to compare the same advice, I guess... (modulo inlining behaviour)
        return GlobalAspectInfo.PRECEDENCE_NONE;

    }

    // Keep a list of pointcuts that use this; if this is empty then we can get rid
    // of this CFS before actually weaving any code in -- this is used in sharing instances
    // of CFS, as we may actually reassign the CFS used by a pointcut to something that
    // has more free variables, so that we can share it

    private List uses/*<CflowPointcut>*/ = new ArrayList();

    public boolean isUsed()                            { return (!uses.isEmpty()); }
    public void clearUses()                        { uses.clear(); }
    public void addUse(CflowPointcut pc)           { uses.add(pc); }
    public void removeUse(CflowPointcut pc)    { uses.remove(pc); }
    public List/*<CflowPointcut>*/ getUses()   { return uses; }

}
