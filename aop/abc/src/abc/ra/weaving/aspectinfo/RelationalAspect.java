/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Reehan Shaikh
 * Copyright (C) 2007 Eric Bodden
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
package abc.ra.weaving.aspectinfo;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.util.Position;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.util.SingletonList;
import abc.ra.ast.TMFromRelTMDecl_c;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.Per;

/**
 * Aspect-info for a relational aspect.
 *
 * @author Eric Bodden
 */
public class RelationalAspect extends Aspect {

	/** Relational aspect formals. */
	protected final List formals;
	/** Names of tracematch bodies for all contained tracematches generated from relational tracematches (or relatioal advice). */
	protected final List<String> tmBodyMethodNames;

	public RelationalAspect(AbcClass cl, Per p, List formals, List<String> tmBodyMethodNames, Position position) {
		super(cl, p, position);
		this.formals = formals;
		this.tmBodyMethodNames = tmBodyMethodNames;
	}

	/**
	 * Performs necessary code generation/transformations in the backend.
	 * <ul>
	 * <li>Replaces <code>this</code> byt the state variable in the tracematch body.
	 * <li>Fills in the associate method to construct a new aspect object.
	 * <li>Generates an appropriate constructor.
	 * </ul>
	 */
	public void codeGen() {		
		replaceThisByState();
		
        fillInAssociate();
	}

	/**
	 * Fills in the associate method.
	 * Calls the default. 
	 * Returns the newly constructed aspect object. 
	 */
	private void fillInAssociate() {
		SootClass sc = getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName("associate"); //FIXME there could be other methods with the same name
        
        Body body = sm.getActiveBody();
        body.getUnits().clear();
        
        Local retLocal = Jimple.v().newLocal("ret", sc.getType());
        body.getLocals().add(retLocal);
        NewExpr newExpr = Jimple.v().newNewExpr(sc.getType());
        AssignStmt assign = Jimple.v().newAssignStmt(retLocal, newExpr);
        body.getUnits().add(assign);
        
        SootMethodRef constrRef = Scene.v().makeConstructorRef(sc,Collections.EMPTY_LIST);
        SpecialInvokeExpr constrInvoke = Jimple.v().newSpecialInvokeExpr(retLocal, constrRef, Collections.EMPTY_LIST);
        InvokeStmt constrInvokeStmt = Jimple.v().newInvokeStmt(constrInvoke);
        body.getUnits().add(constrInvokeStmt);
        
        ReturnStmt retStmt = Jimple.v().newReturnStmt(retLocal);
        body.getUnits().add(retStmt);
	}

	/**
	 * Replaces all references to <code>this</code> by a reference to the state variable.
	 * The value of this variable is read from the disjunct.
	 * This currently requires the name of the state variable to remain intact!
	 */
	private void replaceThisByState() {
		//for each relational tracematch body, replace accesses to this by accesses to "state"
		for (Iterator<String> bodyNameIter = tmBodyMethodNames.iterator(); bodyNameIter.hasNext();) {
			String name = bodyNameIter.next();
	        SootClass sc = getInstanceClass().getSootClass();
	        SootMethod sm = sc.getMethodByName(name);
	        
	        Body body = sm.getActiveBody();
	        boolean foundLocal = false;
			for (Iterator iterator = body.getUnits().iterator(); iterator.hasNext();) {
				Stmt stmt = (Stmt) iterator.next();

				if(stmt instanceof JAssignStmt) {
					JAssignStmt assignStmt = (JAssignStmt) stmt;
					Value leftOp = assignStmt.getLeftOp();
					if(leftOp instanceof Local) {
						Local local = (Local) leftOp;
						
						if(local.getName().indexOf(TMFromRelTMDecl_c.INTERNAL_STATE_VAR_SUFFIX)>-1) {
							foundLocal = true;
							Local thisLocal = Restructure.getThisLocal(sm);							
							AssignStmt newAssign = Jimple.v().newAssignStmt(thisLocal, local);							
							body.getUnits().insertAfter(new SingletonList(newAssign), assignStmt);
							break;
						}
					}
				}
			}
			if(!foundLocal) {
				throw new RuntimeException("Did not find state-local in body of method "+sm.getName()+"\n"+
						body.getUnits());
			}
		}
	}

	/**
	 * @return the list of relational aspect formals
	 */
	public List getFormals() {
		return formals;
	}

}
