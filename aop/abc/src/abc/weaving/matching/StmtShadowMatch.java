package abc.weaving.matching;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;

/** A "stmt" shadow match
 *  @author Ganesh Sittampalam
 */

public abstract class StmtShadowMatch extends ShadowMatch {
    protected Stmt stmt;

    protected StmtShadowMatch(SootMethod container,Stmt stmt) {
	super(container);
	this.stmt=stmt;
    }

    public ShadowMatch getEnclosing() {
	if(stmt.hasTag(abc.soot.util.InPreinitializationTag.name)) return this;
	return new ExecutionShadowMatch(container);
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
		boolean bDoModify=false;
		{
			Set args=new HashSet(); 
			Iterator it=invokeEx.getArgs().iterator();
			while (it.hasNext()) {
				Value val=(Value)it.next();
				if (!(val instanceof Local)) {
					bDoModify=true;
					break;
				} else {
					if (args.contains(val)) {
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
			   .getMethod().getName().equals(SootMethod.constructorName)) {
			    
			    Stmt prev=(Stmt) statements.getPredOf(stmt);

			    if(prev instanceof AssignStmt && 
			       ((AssignStmt) prev).getRightOp() instanceof NewExpr)
				stmt=prev;
			}

			LocalGeneratorEx lg=new LocalGeneratorEx(body);
			NopStmt nop=Jimple.v().newNopStmt();
			statements.insertBefore(nop, stmt);
			stmt.redirectJumpsToThisTo(nop);
			SootMethod invokedMethod=invokeEx.getMethod();
			List parameterTypes=invokedMethod.getParameterTypes();
			Iterator it=parameterTypes.iterator();
			for (int i=0; i<invokeEx.getArgCount(); i++) {
				Type type=(Type)it.next();
				Value val=invokeEx.getArg(i);
				Local l=soot.jimple.Jimple.v().newLocal("uniqueArgLocal" + (nextUniqueID++), 
					type);
				body.getLocals().add(l);
				//lg.generateLocal(type,
				//		"uniqueArgLocal" + (nextUniqueID++));
				AssignStmt as=Jimple.v().newAssignStmt(l,val);
				statements.insertBefore(as, stmt);
				invokeEx.getArgBox(i).setValue(l);
			}
		}
	}
	private static int nextUniqueID=0;

}
