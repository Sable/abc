/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Pavel Avgustinov
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
package abc.soot.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import abc.main.Debug;
import abc.weaving.aspectinfo.AbcType;

import soot.Body;
import soot.BodyTransformer;
import soot.Hierarchy;
import soot.IntType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.IfStmt;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.util.Chain;

public class InstanceOfEliminator extends BodyTransformer {
	private static void debug(String message) {
		if(abc.main.Debug.v().traceInstanceOfEliminator)
			System.err.println("IOE*** " + message);
    }
	
	private static InstanceOfEliminator instance = new InstanceOfEliminator();
	public static InstanceOfEliminator v() { return instance; }
	
	private static int modified = 0;
	
	/**
	 * (null instanceof T) is false for any T, so if we've determined the instanceOf is always true,
	 * we rewrite it to a non-nullness check (which will hopefully either be cheaper or eliminated
	 * by the nullness analysis).
	 */
	private static void setRhsToTrue(AssignStmt as) {
		debug("Setting rhs of [" + as + "] to nullcheck (lhs of type " + ((InstanceOfExpr)as.getRightOp()).getOp().getType() + ").");
		// invalid jimple:
		// as.setRightOp(Jimple.v().newNeExpr(((InstanceOfExpr)as.getRightOp()).getOp(), NullConstant.v()));
		// Real work is done in the main method..
		modified++;
	}
	
	private static void setRhsToFalse(AssignStmt as) {
		debug("Setting rhs of [" + as + "] to false (lhs of type " + ((InstanceOfExpr)as.getRightOp()).getOp().getType() + ").");
		as.setRightOp(IntConstant.v(0));
		modified++;
	}
	
// no state to reset
//  public static void reset() { instance = new InstanceOfEliminator(); }
	
	/**
	 * The basic premise of the InstanceOfEliminator is that we would like to replace
	 *
	 * $z0 = $r0 instanceof SomeType;
	 * 
	 * in Jimple with "$z0 = 0" or "$z0 = 1" whenever possible, and then run constant
	 * propagation and dead code elimination.
	 * 
	 * Locals in Jimple are, of course, typed, so suppose we're looking at the expression
	 * "$r1 instanceof T2", where the type of $r1 is T1.
	 * 
	 * - If both T1 and T2 are classes, then
	 * 		- if T1 is a subtype of T2, the result is always true.
	 * 		- else if T2 is not a subtype of T1, the result is always false.
	 * 
	 * - If T1 is a class and T2 is an interface, then
	 * 		- if T1 implements T2, the instanceof is always true.
	 * 		- else if T1 has no subtype that implements T2, the result is always false. (*)
	 * 
	 * - If T1 is an interface and T2 is a class, then
	 * 		- if there exists no class that implements T1 and is a subtype of T2, the
	 * 			result is always false. (*)
	 * 		- else, if there exists no class that implements T1 and is *not* a subtype of
	 * 			T2, the result is always true. (*)
	 * 
	 * - If both T1 and T2 are interfaces, then
	 * 		- if T1 extends/implements T2, the result is always true.
	 * 		- else if every class that implements T1 also implements T2, the result is always true. (*)
	 * 		- else if no class that implements T1 also implements T2, the result is always false. (*)
	 * 
	 * All the conditions that require enumerating all subtypes/implementers of some type T (marked with
	 * (*) above) are only valid if the compiled program doesn't use dynamic class loading; if it does,
	 * then we cannot know at compile-time the full set of subtypes/implementers. Thus, they are only
	 * applied if the debug flag assumeNoDynamicLoading is set.
	 * 
	 * Note that we could also use a points-to analysis to estimate the set of objects $r1 could
	 * point to, and then, if for each of these the instanceof check gives the same answer, we can
	 * statically evaluate it... however, this is more expensive (probably not beneath -O3), and it
	 * is unclear it will give much improvement.
	 * 
	 */
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		Chain statements = b.getUnits();

		modified = 0;
		
		List<AssignStmt> replaceWithTrue = new ArrayList<AssignStmt>();
		
		for(Iterator it = statements.iterator(); it.hasNext(); ) {
			Stmt stmt = (Stmt) it.next();
			if(stmt instanceof AssignStmt) {
				AssignStmt as = (AssignStmt)stmt;
				if(as.getRightOp() instanceof InstanceOfExpr) {
					InstanceOfExpr inst = (InstanceOfExpr)as.getRightOp();
					Type left = inst.getOp().getType();
					Type right = inst.getCheckType();
					if(!(right instanceof RefType && left instanceof RefType)) {
						debug("Encountered instanceof on non-reftype in [" + stmt + "], continuing...");
						continue;
					} else {
						SootClass leftClass = ((RefType) left).getSootClass();
						SootClass rightClass = ((RefType) right).getSootClass();
						
						Hierarchy types = Scene.v().getActiveHierarchy();
						
						if(!leftClass.isInterface() && !rightClass.isInterface()) {
							// So both are proper classes... Two cases. Either the lhs is a subtype
							// of the rhs, in which case the instanceof is always true, or the above
							// is false and furthermore the rhs is not a subtype of the lhs, in which
							// case the result is always false.
							
							// We handle this by finding the least common supertype of the two types.
							// Note that the cast to RefType should be safe, since we know both left
							// and right are RefTypes.
							RefType lcs = (RefType)left.merge(right, Scene.v());
							if(lcs.equals(right)) {
								// lhs subtype of rhs
								replaceWithTrue.add(as);
								continue;
							} else if(!lcs.equals(left)) {
								// lhs not subtype of rhs and rhs not subtype of lhs
								setRhsToFalse(as);
								continue;
							}
						} else if(!leftClass.isInterface()) {
							// lhs is a class, rhs an interface...
							if(leftClass.getInterfaces().contains(rightClass)) {
								replaceWithTrue.add(as);
								continue;
							}
							if(Debug.v().assumeNoDynamicLoading) {
								// Check if there is some subtype that might possibly implement the interface
								List<SootClass> candidates = types.getImplementersOf(rightClass);
								boolean isFalse = true;
								for(SootClass cl : candidates) {
									if(types.isClassSubclassOfIncluding(cl, leftClass)) {
										isFalse = false;
										break;
									}
								}
								if(isFalse)
									setRhsToFalse(as);
							}
						} else if(!rightClass.isInterface()) {
							// lhs an interface, rhs a class...
							if(Debug.v().assumeNoDynamicLoading) {
								List<SootClass> candidates = types.getImplementersOf(leftClass);
								boolean isTrue = !candidates.isEmpty(), isFalse = true;
								for(SootClass cl : candidates) {
									isTrue &= (types.isClassSubclassOf(cl, rightClass));
									isFalse &= !(types.isClassSubclassOf(cl, rightClass));
									if(!isTrue && !isFalse)
										break;
								}
								// Can't both be true..
								if(isTrue)
									replaceWithTrue.add(as);
								else if(isFalse)
									setRhsToFalse(as);
							}
						} else /*if (leftClass.isInterface() && rightClass.isInterface())*/ {
							if(leftClass.equals(rightClass) ||
									types.isInterfaceSubinterfaceOf(leftClass, rightClass)) {
								replaceWithTrue.add(as);
							}
							if(Debug.v().assumeNoDynamicLoading) {
								List<SootClass> candidates = types.getImplementersOf(leftClass);
								boolean isTrue = !candidates.isEmpty(), isFalse = true;
								for(SootClass cl : candidates) {
									isTrue &= (cl.getInterfaces().contains(rightClass));
									isFalse &= !(cl.getInterfaces().contains(rightClass));
									if(!isTrue && !isFalse)
										break;
								}
								// Can't both be true..
								if(isTrue)
									replaceWithTrue.add(as);
								else if(isFalse)
									setRhsToFalse(as);
							}
						}
					}
				}
			}
		}

		// When an instanceof test looks statically true, it could still be false at runtime if
		// the local is null. Thus, such tests are replaced with nullness checks (which can
		// hopefully be eliminated later).
		//
		// Unfortunately, Jimple doesn't allow (z1 = r1 == null), so we need to use jumps for the
		// assignment, which means we can't do it while iterating the chain above due to
		// safe iteration concerns.
		
		for(AssignStmt as : replaceWithTrue) {
			setRhsToTrue(as);
			Value local = ((InstanceOfExpr)as.getRightOp()).getOp();
			Stmt labelElse = Jimple.v().newNopStmt();
			Stmt labelEndIf = Jimple.v().newNopStmt();
			Stmt ifStmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(local, NullConstant.v()), labelElse);
			Stmt assignTrue = Jimple.v().newAssignStmt(as.getLeftOp(), IntConstant.v(1));
			Stmt gotoStmt = Jimple.v().newGotoStmt(labelEndIf);
			Stmt assignFalse = Jimple.v().newAssignStmt(as.getLeftOp(), IntConstant.v(0));
			statements.insertAfter(ifStmt, as);
			statements.insertAfter(assignTrue, ifStmt);
			statements.insertAfter(gotoStmt, assignTrue);
			statements.insertAfter(labelElse, gotoStmt);
			statements.insertAfter(assignFalse, labelElse);
			statements.insertAfter(labelEndIf, assignFalse);
			statements.remove(as);
		}
		
		// Should we manually run dead code elimination?
		if(modified > 0) debug("Modified a total of " + modified + " expressions.");
	}
}
