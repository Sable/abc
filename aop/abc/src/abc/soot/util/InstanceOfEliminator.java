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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import abc.weaving.aspectinfo.AbcType;

import soot.Body;
import soot.BodyTransformer;
import soot.IntType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.Type;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.IfStmt;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;
import soot.util.Chain;

public class InstanceOfEliminator extends BodyTransformer {
	private static void debug(String message) {
		if(abc.main.Debug.v().instanceOfEliminator)
			System.err.println("IOE*** " + message);
    }
	
	private static InstanceOfEliminator instance = new InstanceOfEliminator();
	public static InstanceOfEliminator v() { return instance; }
	
// no state to reset
//  public static void reset() { instance = new InstanceOfEliminator(); }
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		Chain statements = b.getUnits();

		int modified = 0;
		
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
						// If we have an interface somewhere, we need to be careful... we can either
						// be conservative, or try to figure out if all classes implementing the
						// interface are (in)compatible. We do the easier thing for now:
						// set to true if the rhs is a superinterface of the lhs (or the lhs implements
						// the rhs)
						if(leftClass.isInterface() || rightClass.isInterface()) {
							if(leftClass.getInterfaces().contains(rightClass)) {
								debug("Setting rhs of [" + stmt + "] to true (lhs of type " + left + ").");
								as.setRightOp(IntConstant.v(1));
								modified++;
								continue;
							}
						} else {
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
								debug("Setting rhs of [" + stmt + "] to true (lhs of type " + left + ").");
								as.setRightOp(IntConstant.v(1));
								modified++;
								continue;
							} else if(!lcs.equals(left)) {
								// lhs not subtype of rhs and rhs not subtype of lhs
								debug("Setting rhs of [" + stmt + "] to false (lhs of type " + left + ").");
								as.setRightOp(IntConstant.v(0));
								modified++;
								continue;
							}
						}
					}
				}
			}
		}
		// Should we manually run dead code elimination?
		if(modified > 0) debug("Modified a total of " + modified + " expressions.");
	}
}
