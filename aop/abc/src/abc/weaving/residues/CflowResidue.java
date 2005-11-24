/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Damien Sereni
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

package abc.weaving.residues;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import java.util.*;
import abc.weaving.weaver.*;

/** A dynamic residue for cflow and cflow below
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @author Damien Sereni
 */
public class CflowResidue extends Residue {
    public int id;
    public static int nextId = 0;
    protected CflowSetup setup;
    protected List/*<WeavingVar>*/ vars;

    public Residue inline(ConstructorInliningMap cim) { 
        return new CflowResidue(setup, WeavingVar.inline(vars, cim));
    }
    public Residue optimize() { 
        ArrayList newVars = new ArrayList();
        newVars.addAll(vars);
        return new CflowResidue(setup, newVars);
    }

    public CflowResidue(CflowSetup setup,List vars) {
        this.id = nextId++;
        this.setup=setup;
        this.vars=vars;
    }

    public Residue resetForReweaving() {
        if (vars!=null)
            for( Iterator variableIt = vars.iterator(); variableIt.hasNext(); ) {
                final WeavingVar variable = (WeavingVar) variableIt.next();
                if (variable!=null)
                    variable.resetForReweaving();
            }
        return this;
    }

   public static void debug(String message)
     { if (abc.main.Debug.v().residueCodeGen)
          System.err.println("RCG: "+message);
     }

    protected Stmt isValidStmt;
    /** Returns the statement that was woven to test isValid(). */
    public Stmt getIsValidStmt() { return isValidStmt; }

    protected Object insertChainAfter(Chain toInsertInto, Chain toInsert, Object insertAfterThis) {
    	Iterator it = toInsert.iterator();
    	Object last = insertAfterThis;
    	while (it.hasNext()) {
    		Object o = it.next();
    		toInsertInto.insertAfter(o, last);
    		last = o;
    	}
    	return last;
    }
    
    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {

        CflowCodeGenUtils.CflowCodeGen codegen = setup.codeGen();
        if(wc.getKindTag() == null) {
            // kind is not CFLOW_TEST
            wc.setKindTag(InstructionKindTag.ADVICE_TEST);
        }
        debug("beginning cflow codegen " + id);

        Type object=Scene.v().getSootClass("java.lang.Object").getType();

        Stmt last;
        
        debug("getting cflow state");
        Local cflowStack = setup.getMethodCflowLocal(localgen, method);
        last = begin;

        // If once-per-method retrieval is disabled, make a new local for cflowLocal,
        // and use genInitLocal instead of genInitLocalLazily
        Local cflowLocal = 
        	(abc.main.options.OptionsParser.v().cflow_share_thread_locals() ?
        		setup.getMethodCflowThreadLocal(localgen, method)
        			:
        		localgen.generateLocal(setup.codeGen().getCflowInstanceType(), "cflowThreadLocal"));

        Chain getlocalstack = 
    		(abc.main.options.OptionsParser.v().cflow_share_thread_locals() ? 
    				setup.codeGen().genInitLocalLazily(localgen, cflowLocal, cflowStack)
    						:
    				setup.codeGen().genInitLocal(localgen, cflowLocal, cflowStack));
        Tagger.tagChain(getlocalstack, wc);
        last = (Stmt)insertChainAfter(units, getlocalstack, last);
        
        debug("checking validity");
        	// Finding the succeed and fail statements
        	// If sense, fail is the fail param, and
        	// succeed is the first statement after the abort test (insert a nop for this purpose)
         	// Otherwise, reversed
        Stmt failStmt; Stmt succeedStmt;
        Stmt afterAbort = Jimple.v().newNopStmt();
        if (sense) { failStmt = fail; succeedStmt = afterAbort; } 
        else { failStmt = afterAbort; succeedStmt = fail;  }
        
        Local isvalid=localgen.generateLocal(BooleanType.v(),"cflowactive");

        ChainStmtBox isValidCheck = codegen.genIsValid(localgen, cflowLocal, isvalid, succeedStmt, failStmt);
        Tagger.tagChain(isValidCheck.getChain(), wc);
        last = (Stmt)insertChainAfter(units, isValidCheck.getChain(), last);
        isValidStmt = isValidCheck.getStmt();

        debug("generating abort");
        Expr test;
        if(sense) test=Jimple.v().newEqExpr(isvalid,IntConstant.v(0));
        else test=Jimple.v().newNeExpr(isvalid,IntConstant.v(0));
        Stmt abort=Jimple.v().newIfStmt(test,fail);
        Tagger.tagStmt(abort, wc);
        units.insertAfter(abort,last);
        units.insertAfter(afterAbort, abort);

        last = afterAbort;

        if (sense && vars.size() > 0) {
        	
        debug("setting up to get bound values");

        List reslocals = new LinkedList();
        List realwvars = new LinkedList();
        Iterator it = vars.iterator();
        while (it.hasNext()) {
        	WeavingVar to = (WeavingVar) (it.next());
        	if (to != null) {
            	Local newLocal = localgen.generateLocal(to.getType(), "cflowbound");
        		reslocals.add(newLocal);
        		realwvars.add(to);
        	}

        }
        
        debug("get bound values");
        Chain peekchain = codegen.genPeek(localgen, cflowLocal, reslocals);
        Tagger.tagChain(peekchain, wc);
        last = (Stmt)insertChainAfter(units,peekchain, last);
        
        debug("Copy bound values into weaving vars");
        it = realwvars.iterator();
        Iterator it2 = reslocals.iterator();
        while (it.hasNext()) {
        	WeavingVar to = (WeavingVar) it.next();
        	Local res = (Local)it2.next();
        	last = to.set(localgen, units, last, wc, res);
        	// last.addTag(InstructionKindTag.ADVICE_TEST)
        }

        }
        
        debug("done with cflow codegen");
        return last;

    }

    public String toString() {
        return "cflow("+setup.getPointcut()+")";
    }

    public CflowSetup setup() { return setup; }

}
