/* abc - The AspectBench Compiler
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

package abc.weaving.matching;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import polyglot.util.InternalCompilerError;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;
import soot.util.Chain;
import abc.main.Debug;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.Residue;

/** A base class for join point shadows that apply to a single statement
 *  (or pair of statements in the case of constructor calls)
 *  @author Ganesh Sittampalam
 */

public abstract class StmtShadowMatch extends ShadowMatch {
    protected Stmt stmt;

    public Host getHost() {
		return stmt;
	}
	protected AdviceApplication doAddAdviceApplication(MethodAdviceList mal,
			AbstractAdviceDecl ad, Residue residue) {
		StmtAdviceApplication aa = new StmtAdviceApplication(ad,residue,stmt);
        mal.addStmtAdvice(aa);
        return aa;
	}
	
    protected StmtShadowMatch(SootMethod container,Stmt stmt) {
        super(container);
        this.stmt=stmt;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public ShadowMatch getEnclosing() {
        if(stmt.hasTag(abc.soot.util.InPreinitializationTag.name)
           && abc.main.Debug.v().ajcCompliance) return this;
        return ExecutionShadowMatch.construct(container);
    }

    public ContextValue getThisContextValue() {
        try {
            if(stmt.hasTag(abc.soot.util.InPreinitializationTag.name)) return null;
        } catch(NullPointerException e) {
            throw new InternalCompilerError("NPE while looking for tags on stmt "+stmt,e);
        }
        return super.getThisContextValue();
    }

        /**
         * Lazily replaces the arguments of the invokeExpr of stmt with
         * unique locals and inserts assignment statements before stmt,
         * assigning the original values to the locals.
         * Needed for around().
         * @param method
         * @param stmt
         */
        public static void makeArgumentsUniqueLocals(SootMethod method, Stmt stmt) {
                InvokeExpr invokeEx=stmt.getInvokeExpr();

                SootMethodRef invokedMethod=invokeEx.getMethodRef();
                List parameterTypes=invokedMethod.parameterTypes();

                boolean bDoModify=false;
                {

                        Set args=new HashSet();
                        Iterator it=invokeEx.getArgs().iterator();
                        Iterator itType=parameterTypes.iterator();
                        while (it.hasNext()) {
                                Type type=(Type)itType.next();
                                Value val=(Value)it.next();
                                if (!(val instanceof Local)) {
                                        bDoModify=true;
                                        break;
                                } else {
                                        Local l=(Local)val;
                                        if (args.contains(val)) {
                                                bDoModify=true;
                                                break;
                                        }
                                        // The local must have the type of the formal of the method.
                                        //
                                        if (!l.getType().equals(type)) {
                                                bDoModify=true;
                                                break;
                                        }
                                        args.add(val);
                                }
                        }
                        }
                if (bDoModify) {
                        Body body=method.getActiveBody();
                        Chain statements=body.getUnits().getNonPatchingChain();

                        // If this is a new+constructor pair, we want to put the moved stuff before
                        // the new
                        if(stmt instanceof InvokeStmt &&
                           ((InvokeStmt) stmt).getInvokeExpr()
                           .getMethodRef().name().equals(SootMethod.constructorName)) {

                            Stmt prev=(Stmt) statements.getPredOf(stmt);

                            if(prev instanceof AssignStmt &&
                               ((AssignStmt) prev).getRightOp() instanceof NewExpr)
                                stmt=prev;
                        }

                        LocalGeneratorEx lg=new LocalGeneratorEx(body);
                        NopStmt nop=Jimple.v().newNopStmt();
                        statements.insertBefore(nop, stmt);
                        stmt.redirectJumpsToThisTo(nop);

                        Iterator it=parameterTypes.iterator();
                        for (int i=0; i<invokeEx.getArgCount(); i++) {
                                Type type=(Type)it.next();
                                Value val=invokeEx.getArg(i);
                                Local l=soot.jimple.Jimple.v().newLocal("uniqueArgLocal" + (nextUniqueID++),
                                        type);
                                body.getLocals().add(l);
                                //lg.generateLocal(type,
                                //              "uniqueArgLocal" + (nextUniqueID++));
                                AssignStmt as=Jimple.v().newAssignStmt(l,val);
                                statements.insertBefore(as, stmt);
                                invokeEx.getArgBox(i).setValue(l);
                        }
                }
        }
        private static int nextUniqueID=0;
        public static void reset() {
                nextUniqueID=0;
        }
}
