/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.JimpleBodyPack;
import soot.Local;
import soot.PrimType;
import soot.Scene;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import soot.jimple.*;
import soot.*;
import java.util.*;

import polyglot.util.InternalCompilerError;

/**
 * @author Sascha Kuzins
 *
 */
public class BoxingRemover extends BodyTransformer {
	
	public static BoxingRemover v() {
		return new BoxingRemover();
	}
	
	static private void debug(String message) {
		if (abc.main.Debug.v().boxingRemover)
			System.err.println("BXR*** " + message);
	}

	
	private static class BoxingTypeLocal {
		public BoxingTypeLocal(Local l) {
			local=l;
			primType=
				Restructure.JavaTypeInfo.getBoxingClassPrimType( 
						((RefType)l.getType()).getSootClass());
		}
		public final Type primType;
		public final Local local;
		public AssignStmt newStmt=null;
		public InvokeStmt initStmt=null;
		public Value initValue=null;
		public List  valueStmts=new LinkedList();
	}
	
	private static void removeUnnecessaryCasts(Body body) {
		Chain statements=body.getUnits();                
        for (Iterator stmtIt=statements.iterator(); stmtIt.hasNext(); ) {
        	Stmt stmt=(Stmt)stmtIt.next();
        	
        	if (stmt instanceof AssignStmt) {
        		AssignStmt as=(AssignStmt)stmt;
        		Type leftType=as.getLeftOp().getType();
        		if (as.getRightOp() instanceof CastExpr) {
        			CastExpr ce=(CastExpr)as.getRightOp();
        			if (ce.getOp().getType().equals(leftType)) {
        				debug("Removing unnecessary cast: " + stmt);
        				as.setRightOp(ce.getOp());
        			}
        		}
        	}
        }
	}
	

	private static void runJopPack(Body body) {
		for (Iterator it=PackManager.v().getPack("jop").iterator(); it.hasNext();) {
			Transform t=(Transform)it.next();
			t.apply(body);
		}
	}
	private static void runPreOptimizations(Body body) {
		runJopPack(body);
		removeUnnecessaryCasts(body);
		runJopPack(body);	
	}
//	 returns number of removed cases of boxing
	protected void internalTransform(Body body, String phaseName, Map options) {
	//public static int removeUnnecessaryBoxing() {
		
		int removedBoxings=0;
		
		SootMethod method=body.getMethod();
        
            
        Map boxingTypeLocals=new HashMap();
        
        Chain locals=body.getLocals();
        for (Iterator itLoc=locals.iterator(); itLoc.hasNext();) {
        	soot.Local l=(soot.Local)itLoc.next();
        	if (Restructure.JavaTypeInfo.isBoxingType(l.getType())) {
        		boxingTypeLocals.put(l,
        				new BoxingTypeLocal(l)
        		);
        		debug("Found boxing local: " + l);
        	}
        }
        if (boxingTypeLocals.isEmpty())
        	return;             
        
        runPreOptimizations(body);
        
        Chain statements=body.getUnits();                
        for (Iterator stmtIt=statements.iterator(); stmtIt.hasNext(); ) {
        	Stmt stmt=(Stmt)stmtIt.next();
        	
        	// go through all use boxes of this stmt
        	for (Iterator itUse=stmt.getUseBoxes().iterator(); itUse.hasNext();) {
        		ValueBox box=(ValueBox)itUse.next();
        		BoxingTypeLocal pl=(BoxingTypeLocal)boxingTypeLocals.get(box.getValue());
        		
        		// if one of the boxing type locals is used
        		if (pl!=null) {
        			Local l=pl.local;
        			
        			// make sure it's used according to our restriction, else
        			// remove it from the map.
        			
        			// first valid use: <init> call
        			if (stmt instanceof InvokeStmt &&
        				stmt.containsInvokeExpr() &&
        			    stmt.getInvokeExpr() instanceof SpecialInvokeExpr &&
        			    ((SpecialInvokeExpr)stmt.getInvokeExpr()).getBase().equals(l) &&
						stmt.getInvokeExpr().getMethodRef().name().equals("<init>") &&
						stmt.getInvokeExpr().getMethodRef().parameterTypes().size()==1 &&
						stmt.getInvokeExpr().getMethodRef().parameterType(0) instanceof PrimType) {
        			
        				// if initialized twice, we can't optimize it away.
        				// should usually not occur in Jimple 
        				if (pl.initStmt!=null) {
        					boxingTypeLocals.remove(l);
        					debug("Found two initializations for same local: " + l);
        				} else {
        					pl.initStmt=(InvokeStmt)stmt;
        					pl.initValue=stmt.getInvokeExpr().getArg(0);
        				}
        				
        			// second valid use: call to longValue()/intValue() etc.
        			} else if (stmt.containsInvokeExpr() &&
            			    stmt.getInvokeExpr() instanceof VirtualInvokeExpr &&
            			    ((VirtualInvokeExpr)stmt.getInvokeExpr()).getBase().equals(l) &&
							stmt.getInvokeExpr().getMethodRef().name().equals(
								Restructure.JavaTypeInfo.getBoxingClassMethodName(l.getType())
								) &&
							stmt.getInvokeExpr().getMethodRef().parameterTypes().size()==0) {
        				
        				pl.valueStmts.add(stmt);
        			} else {
        				// invalid use. remove local from map.
        				boxingTypeLocals.remove(l);
        				debug(" Invalid use of local " + l + ": " + stmt);
        			}
        		}
        	} // use boxes
			
//                	 go through all def boxes of this stmt
        	for (Iterator itDef=stmt.getDefBoxes().iterator(); itDef.hasNext();) {
        		ValueBox box=(ValueBox)itDef.next();
        		BoxingTypeLocal pl=(BoxingTypeLocal)boxingTypeLocals.get(box.getValue());
        		// if one of the boxing type locals is used
        		if (pl!=null) {
        			Local l=pl.local;
        			// make sure it's used according to our restriction, else
        			// remove it from the map.
        			
        			// valid def use: instantiation
        			if (stmt instanceof AssignStmt &&
        				((AssignStmt)stmt).getRightOp() instanceof soot.jimple.NewExpr &&
        				((NewExpr)((AssignStmt)stmt).getRightOp()).getBaseType().equals(
        						l.getType()) &&
						((AssignStmt)stmt).getLeftOp().equals(l)) {
        				
//                				 if initialized twice, we can't optimize it away.
        				// should usually not occur in Jimple 
        				if (pl.newStmt!=null) {
        					boxingTypeLocals.remove(l);
        					debug("Found two new expressions for same local: " + l);
        				} else
        					pl.newStmt=(AssignStmt)stmt;
        				
        			} else {
//                				 invalid def use. remove local from map.
        				boxingTypeLocals.remove(l);
        				debug(" Invalid def use of local " + l + ": " + stmt);
        			}
        		}
        	} //def boxes
        } // statements
        for (Iterator itL=boxingTypeLocals.values().iterator(); itL.hasNext();) {
        	BoxingTypeLocal pl=(BoxingTypeLocal)itL.next();
        	if (pl.initStmt==null)
        		continue;
        	if (pl.newStmt==null)
        		continue;
        	
        	debug("Removing boxing case.");
        	debug("	Method: " + method);
        	debug("	Local: " + pl.local);
        	
        	locals.remove(pl.local);
        	
        	statements.remove(pl.newStmt);
        	if (pl.valueStmts.isEmpty()) {
        		statements.remove(pl.initStmt);                		
        		continue;
        	} else {
        		LocalGeneratorEx lg=new LocalGeneratorEx(body);
        		Local newLocal=lg.generateLocal(pl.primType);
        		// assign former initialization value to new local
        		
        		//pl.initStmt.setRightOp(pl.initValue);
        		//pl.initStmt.setLeftOp(newLocal);
        		AssignStmt newAs=Jimple.v().newAssignStmt(newLocal, pl.initValue);
        		statements.insertAfter(
        				newAs,
        				pl.initStmt);
        		pl.initStmt.redirectJumpsToThisTo(newAs);
        		statements.remove(pl.initStmt);
        		
        		for (Iterator itV=pl.valueStmts.iterator(); itV.hasNext();) {                	
        			Stmt s=(Stmt) itV.next();
        			if (s instanceof AssignStmt) {
        				AssignStmt as=(AssignStmt)s;
        				as.setRightOp(newLocal);
        			} else if (s instanceof InvokeStmt) {
        				statements.remove(s);
        			} else {
        				throw new InternalCompilerError("unexpected statement type: " + s);
        			}
        		}	
        	}                	
        }                
	}
}
