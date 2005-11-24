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
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;
import abc.weaving.tagkit.Tagger;
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
    
    private CflowCodeGenUtils.CflowCodeGen codeGen = null;
    public CflowCodeGenUtils.CflowCodeGen codeGen() { 
    	if (codeGen == null)
    		// Init the codegen. This occurs when the cflow stack is first added to the aspect
    		getCflowStack();
    	return codeGen; }
    
    private CflowSetup(Aspect aspct,Pointcut pc,boolean isBelow,
                       List/*<Formal>*/ formals,List/*<Var>*/ actuals,
                       Position pos,int depth) {
        super(aspct,new BeforeAfterAdvice(pos),pc,formals,pos);
        this.actuals=actuals;
        this.isBelow=isBelow;
        this.depth=depth;

    }

    public boolean isBelow() {
        return isBelow;
    }

    public int getDepth() {
	if(depth==-1) throw new InternalCompilerError("depth of CflowSetup was -1",getPosition());
        return depth;
    }

    public List/*<Var>*/ getActuals() {
        return actuals;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
        sb.append(prefix+" type: "+spec+"\n");
        sb.append(prefix+" pointcut: "+pc+"\n");
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
        private Local loc=null;
        
        public WeavingVar inline(ConstructorInliningMap cim) {
        	return new CflowSetupBound(pos, type);
        }

        public void resetForReweaving() {
            loc=null;
        }
        
        public CflowSetupBound(int pos,Type type) {
            this.pos=pos;
            this.type=type;
        }

        public String toString() {
            return "cflowbound("+pos+":"+type+")";
        }

        public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,
                        WeavingContext wc,Value val) {
            if(loc==null) loc = localgen.generateLocal(type,"adviceformal");
            Stmt assignStmt=Jimple.v().newAssignStmt(loc,val);
            Tagger.tagStmt(assignStmt, wc);
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

            CflowSetupBound setupbound=new CflowSetupBound(index,type);

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
        popStmts=new HashMap();
        pushStmts=new HashMap();
        methodCflowLocals = new HashMap();
        methodCflowThreadLocals = new HashMap();
    }
    
    private SootFieldRef cflowStack=null; private SootMethod preClinit=null;
    public SootFieldRef getCflowStack() {
        if(cflowStack==null) {

        	// First, init the cflow codegen
            List/*<Type>*/ formalTypes = new LinkedList();
            for (Iterator it = getFormals().iterator(); it.hasNext(); 
        		formalTypes.add(((Formal)it.next()).getType().getSootType()));
            codeGen = CflowCodeGenUtils.CflowCodeGenFactory.v(formalTypes);
        	
            SootClass cl=getAspect().getInstanceClass().getSootClass();

            int i=0;
            String name;
            do {
                name="abc$"+codeGen.chooseName()+"$"+i;
                i++;
            } while(cl.declaresFieldByName(name));
            
            SootField cflowStackF=new SootField(name,codeGen.getCflowType(),
                                                Modifier.PUBLIC | Modifier.STATIC);
            cl.addField(cflowStackF);
            cflowStack=cflowStackF.makeRef();
   
            codeGen.setCflowField(cflowStack);


            SootMethod preClinit=new AspectCodeGen().getPreClinit(cl);
            LocalGeneratorEx lg=new LocalGeneratorEx
                (preClinit.getActiveBody());
            this.preClinit = preClinit;

            Chain units=preClinit.getActiveBody().getUnits();

            Stmt returnStmt=(Stmt) units.getFirst();
            while(!(returnStmt instanceof ReturnVoidStmt))
                returnStmt=(Stmt) units.getSuccOf(returnStmt);
            
            Chain cflowInitStmts = codeGen.genInitCflowField(lg, cflowStack);
           
            
            Iterator it = cflowInitStmts.iterator();
            while (it.hasNext()) {
            	Stmt s = (Stmt)it.next();
            	units.insertBefore(s, returnStmt);
            }
            
        }
        return cflowStack;
    }

    private Map/*SootMethod->Local*/ methodCflowLocals = new HashMap();
    private Map/*SootMethod->Local*/ methodCflowThreadLocals = new HashMap();
    
    /** Return the first statement in a method after which it is safe to insert
     *  the initialisation of a cflow local.
     */
    private Stmt getMethodFirstSafeCflowStatement(SootMethod m) {
    	// Note: the cflow field is initialised in the preClInit of the aspect
    	// that this advice belongs to.
    	// The only problem is therefore if m is the <init> of this aspect - then
    	// before the call the preClInit the cflow field is not initialised. 
    	// preClInit is not weavable, so no problem there.

	// Also avoid any initialisation statements before the IdentityStmts at the
	// beginning of the method.
    	SootClass cl = m.getDeclaringClass();
    	if (cl.equals(getAspect().getInstanceClass().getSootClass())
    			&&
			m.getName().equals(SootMethod.staticInitializerName)) {
    		// m is the <init> of this aspect...
    		
    		// Find the (first and hopefully only) call to preClinit
    		Iterator it = m.getActiveBody().getUnits().iterator();
    		while (it.hasNext()) {
    			Stmt s = (Stmt)it.next();
    			if (s.containsInvokeExpr()) {
    				InvokeExpr ie = s.getInvokeExpr();
    				if (ie.getMethod().equals(preClinit))
						return s;
    			}
    		}
    		throw new RuntimeException("preClinit not found in aspect init "+m);
    	} else {
    		// m is a safe method. Add a nop at the beginning for definiteness
    		// and return it
		Iterator it = m.getActiveBody().getUnits().iterator();
		Stmt s = null;
		// Skip over the IdentityStmts
		while (it.hasNext()) {
		    Stmt news = (Stmt)it.next();
		    if (!(news instanceof IdentityStmt)) break;
		    s = news;
		}
    		if (s == null) {
		    Stmt nop = Jimple.v().newNopStmt();
		    m.getActiveBody().getUnits().addFirst(nop);
		    return nop;
		} else {
		    // The first safe stmt to insert after is the last
		    // identitystmt
		    return s;
		}
    	}
    }
    
    public Local getMethodCflowLocal(LocalGeneratorEx localgen, SootMethod m) {
    	Local l = (Local)methodCflowLocals.get(m);
    	if (l != null) return l;
    	
    	l = localgen.generateLocal(codeGen().getCflowType(), codeGen().chooseName());
    	Stmt getIt = Jimple.v().newAssignStmt(l, Jimple.v().newStaticFieldRef(getCflowStack()));
        Tagger.tagStmt(getIt, InstructionKindTag.GET_CFLOW_LOCAL);
    	Stmt insertAfter = 
    		getMethodFirstSafeCflowStatement(m);
		m.getActiveBody().getUnits().insertAfter(getIt, insertAfter);
		
    	methodCflowLocals.put(m, l);
    	return l;
    }
    public Local getMethodCflowThreadLocal(LocalGeneratorEx localgen, SootMethod m) {
    	Local l = (Local)methodCflowThreadLocals.get(m);
    	if (l != null) return l;

    	l = localgen.generateLocal(codeGen().getCflowInstanceType(), codeGen().chooseName()+"Instance");
		
		Chain c = codeGen().genInitLocalToNull(localgen, l);
        Tagger.tagChain(c, InstructionKindTag.GET_CFLOW_THREAD_LOCAL);

		Stmt insertAfter = getMethodFirstSafeCflowStatement(m);

		Iterator it = c.iterator();
		Chain body = m.getActiveBody().getUnits();

		while (it.hasNext()) {
			Stmt s = (Stmt)it.next();
			body.insertAfter(s, insertAfter);
			insertAfter = s;
		}
		methodCflowThreadLocals.put(m, l);
		
		return l;
    }
    
    public Map/*AdviceApplication->Stmt*/ popStmts = new HashMap();
    public Map/*AdviceApplication->Stmt*/ pushStmts = new HashMap();

    public Chain makeAdviceExecutionStmts
         (AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {
    	
        CflowSetupWeavingContext cswc=(CflowSetupWeavingContext) wc;
        
        // Prepare codeGen()
        Chain c = new HashChain();
        
        //FIXME Getting the current method is a hack, but fixing it requires adding it as a param
        // Get current method
        SootMethod m = localgen.getMethod();
        
        // Get the cflow class and thread-local
        // The thread-local only needs to be initialised (lazily) for PUSH, never POP
        // (as a POP always occurs after a PUSH, and so the local must be initialised)
        Local cflowInstance = getMethodCflowLocal(localgen, m);

        // If once-per-method retrieval is disabled, make a new local for cflowLocal,
        // and use genInitLocal instead of genInitLocalLazily
        Local cflowLocal = 
        	(abc.main.options.OptionsParser.v().cflow_share_thread_locals() ?
        		getMethodCflowThreadLocal(localgen, m)
        			:
        		localgen.generateLocal(codeGen().getCflowInstanceType(), "cflowThreadLocal"));
        
        if (cswc.doBefore) {
        	// PUSH

        	// Initialise the cflow thread-local
        	Chain getInstance = 
        		(abc.main.options.OptionsParser.v().cflow_share_thread_locals() ? 
        				codeGen().genInitLocalLazily(localgen, cflowLocal, cflowInstance)
        						:
        				codeGen().genInitLocal(localgen, cflowLocal, cflowInstance));
        	c.addAll(getInstance);

        	List/*<Value>*/ values = new LinkedList();
        	Iterator it = cswc.bounds.iterator();
        	while (it.hasNext()) {
        		Value v = (Value)it.next();
        		values.add(v);
        	}
        	
        	ChainStmtBox pushChain = codeGen().genPush(localgen, cflowLocal, values);
        	c.addAll(pushChain.getChain());
        	pushStmts.put(adviceappl, pushChain.getStmt());
            Tagger.tagChain(c, InstructionKindTag.CFLOW_ENTRY);
        	
        } else {
        	// POP
        	
        	// We do have to initialise cflowLocal if cflow_share_thread_locals:false
        	if (!abc.main.options.OptionsParser.v().cflow_share_thread_locals()) {
        		Chain getInstance = 
        			codeGen().genInitLocal(localgen, cflowLocal, cflowInstance);
        		c.addAll(getInstance);
        	}
        	
        	ChainStmtBox popChain = codeGen().genPop(localgen, cflowLocal);
        	c.addAll(popChain.getChain());
        	popStmts.put(adviceappl, popChain.getStmt());
            Tagger.tagChain(c, InstructionKindTag.CFLOW_EXIT);
        }
        
        Tagger.tagChain(c, new InstructionSourceTag(adviceappl.advice.sourceId));
        Tagger.tagChain(c, new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
        return c;

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
            return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence
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
