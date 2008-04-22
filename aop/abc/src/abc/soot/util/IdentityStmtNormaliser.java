/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

import polyglot.util.ErrorInfo;
import polyglot.util.Position;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.toolkits.scalar.LocalPacker;

/**
 * Normalizes IdentityStmts by moving them forward in the method.
 * May yield potential for the {@link LocalPacker} and {@link DeadAssignmentEliminator}.
 * 
 * @author Eric Bodden
 * @author Pavel Avgustinov
 * @author Torbjorn Ekman
 */
public class IdentityStmtNormaliser extends BodyTransformer {

	protected static IdentityStmtNormaliser instance;
	
	/**
	 * Check whether the given body already satisfies the invariant this class aims
	 * to establish, namely that all identity statements are at the start of the unit
	 * chain.
	 */
	protected boolean bodyIsCorrect(Body b) {
		boolean initialIdentities = true;
		for(Unit u : b.getUnits()) {
			if(u instanceof IdentityStmt && !initialIdentities)
				return false;
			if(!(u instanceof IdentityStmt))
				initialIdentities = false;
		}
		return true;
	}
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		if(bodyIsCorrect(b))
			return;
		
		abc.main.Main.v().getAbcExtension().reportError(ErrorInfo.WARNING, 
				"Invalid Jimple body for " + b.getMethod().getSignature() + ":\n" +
				" All identity statements should occur at the start of a Jimple method. abc\n" +
				"will try to fix this, but really it's a bug in code generation.", 
				Position.COMPILER_GENERATED);
		
		LocalGenerator localGen = new LocalGenerator(b);
		
		IdentityStmt thisStmt = null;
		if(!b.getMethod().isStatic()) {		
			//add r0 = @this
			RefType thisType = b.getMethod().getDeclaringClass().getType();
			Local thisLocal = localGen.generateLocal(thisType);
			thisStmt = Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(thisType));
			b.getUnits().addFirst(thisStmt);
		}
		
		//insert parameter assignment statements
		IdentityStmt lastIdStmt = thisStmt;
		for(int i=0; i<b.getMethod().getParameterCount(); i++) {
			Type paramType = b.getMethod().getParameterType(i);
			Local l = localGen.generateLocal(paramType);
			IdentityStmt paramAssign = Jimple.v().newIdentityStmt(l, Jimple.v().newParameterRef(paramType, i));
			if(lastIdStmt!=null)
				b.getUnits().insertAfter(paramAssign, lastIdStmt);
			else 
				b.getUnits().addFirst(paramAssign);
			lastIdStmt = paramAssign;			
		}
		
		//reroute all original identity statements to those at the beginning of the method
		Iterator<Unit> restIter = b.getUnits().snapshotIterator();
		Unit next = restIter.next();
		while(restIter.hasNext() && next!=lastIdStmt) {
			next = restIter.next();
		}
		if(restIter.hasNext()) {
			while(restIter.hasNext()) {
				Unit currUnit = restIter.next();
				if(currUnit instanceof IdentityStmt) {
					IdentityStmt idStmt = (IdentityStmt) currUnit;
					Value rightOp = idStmt.getRightOp();
					if(rightOp instanceof ThisRef) {
						b.getUnits().swapWith(currUnit, Jimple.v().newAssignStmt(idStmt.getLeftOp(), b.getThisLocal()));
					} else if(rightOp instanceof ParameterRef) {
						ParameterRef paramRef = (ParameterRef) rightOp;
						int index = paramRef.getIndex();
						b.getUnits().swapWith(currUnit, Jimple.v().newAssignStmt(idStmt.getLeftOp(), b.getParameterLocal(index)));
					}
				}
			}
		}
	}
	
	public static IdentityStmtNormaliser v() {
		if(instance==null) {
			instance = new IdentityStmtNormaliser();
		}
		return instance;
	}

}