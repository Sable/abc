/*
 * Created on 20-Oct-2004
 *
 */
package abc.soot.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import abc.weaving.weaver.CflowIntraproceduralAnalysis;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.StmtBody;
import soot.jimple.TableSwitchStmt;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.options.Options;
import soot.util.Chain;

/**
 * @author Sascha Kuzins
 *
 */
public class SwitchFolder extends BodyTransformer {

	
	private static SwitchFolder instance = 
		new SwitchFolder();
	public static void reset() { instance = new SwitchFolder(); }
	
	
	public static SwitchFolder v() { return instance; }
	
	// temporary code.
	// need to create SwitchStmt interface and
	// move common members up the hierarchy
	private Value getKey(Stmt stmt) {
		if (stmt instanceof LookupSwitchStmt) {
			LookupSwitchStmt s=(LookupSwitchStmt)stmt;
			return s.getKey();
		} else if (stmt instanceof TableSwitchStmt) {
			TableSwitchStmt s=(TableSwitchStmt)stmt;
			return s.getKey();
		} else 
			throw new RuntimeException("");
	}
	private Unit getTargetOfKey(Stmt stmt, int key) {
		if (stmt instanceof LookupSwitchStmt) {
			LookupSwitchStmt s=(LookupSwitchStmt)stmt;
			Iterator itT=s.getTargets().iterator();
			for (Iterator it=s.getLookupValues().iterator(); it.hasNext();) {
				IntConstant v=(IntConstant)it.next();
				Stmt target=(Stmt)itT.next();
				if (v.value==key) {
					return target;
				}
			}
			return s.getDefaultTarget();			
		} else if (stmt instanceof TableSwitchStmt) {
			TableSwitchStmt s=(TableSwitchStmt)stmt;
			if (key<s.getLowIndex() || key>s.getHighIndex())
				return s.getDefaultTarget();
			else
				return s.getTarget(key-s.getLowIndex());
		} else 
			throw new RuntimeException("");
	}

	protected void internalTransform(Body body, String phaseName, Map options) {


		StmtBody stmtBody = (StmtBody)body;
		
		// ConstantPropagatorAndFolder.v().transform(body); // TODO: phase name? 
		
		if (Options.v().verbose())
            G.v().out.println("[" + stmtBody.getMethod().getName() +
                               "] Folding switch statements...");

		
		
		Chain units = stmtBody.getUnits();
        ArrayList unitList = new ArrayList(); unitList.addAll(units);

        boolean hasFolded=false;
        Iterator stmtIt = unitList.iterator();
        while (stmtIt.hasNext()) {
        	Stmt stmt = (Stmt)stmtIt.next();
            if (stmt instanceof LookupSwitchStmt ||
            	stmt instanceof TableSwitchStmt) {
                // check for constant-valued conditions
            	//LookupSwitchStmt ls=(LookupSwitchStmt) stmt;
                Value cond = getKey(stmt);
                if (Evaluator.isValueConstantValued(cond)) {
                    cond = Evaluator.getConstantValueOf(cond);

                    int val=((IntConstant) cond).value;
                    
                    Unit target=getTargetOfKey(stmt, val);
                    
                    Stmt newStmt =
                            Jimple.v().newGotoStmt(target);
                        
                    units.insertAfter(newStmt, stmt);
                               
                    // remove switch
                    units.remove(stmt);
                    
                    hasFolded=true;
                }
            }
        }
		//if (hasFolded) {
		//	UnreachableCodeEliminator.v().transform(body);
		//	UnconditionalBranchFolder.v().transform(body); // TODO: Phase name?			
		//}
	}

}
