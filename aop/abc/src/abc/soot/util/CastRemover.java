/*
 * Created on 18-Apr-2005
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
