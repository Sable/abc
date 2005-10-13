/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Sascha Kuzins
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

import soot.Body;
import soot.BodyTransformer;
import soot.Scene;
import soot.Type;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.util.Chain;

/**
 * @author sascha
 *
 */
public class CastRemover extends BodyTransformer {

	private static CastRemover instance = 
		new CastRemover();
	public static void reset() { instance = new CastRemover(); }
	
	
	public static CastRemover v() { return instance; }
	
	
	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	protected void internalTransform(Body b, String phaseName, Map options) {
		removeUnnecessaryCasts(b);
	}

	public static void removeUnnecessaryCasts(Body body) {
		Chain statements=body.getUnits();         
		int removed=0;
        for (Iterator stmtIt=statements.iterator(); stmtIt.hasNext(); ) {
        	Stmt stmt=(Stmt)stmtIt.next();
        	
        	if (stmt instanceof AssignStmt) {
        		AssignStmt as=(AssignStmt)stmt;
        		Type leftType=as.getLeftOp().getType();
        		if (as.getRightOp() instanceof CastExpr) {
        			CastExpr ce=(CastExpr)as.getRightOp();        			
        			if (ce.getOp().getType().equals(leftType)) {
        				//debug("Removing unnecessary cast: " + stmt);
        				as.setRightOp(ce.getOp());
        				removed++;
        			}
        		}
        	}
        }
        if (removed>0) {
        	CopyPropagator.v().transform(body);
        	DeadAssignmentEliminator.v().transform(body);
        }
	}
}
