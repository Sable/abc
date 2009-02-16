package abc.impact.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Hierarchy;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.TrapManager;
import soot.Type;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;
import abc.impact.utils.ImpactUtil;

/**
 * This analysis is specific to analyze checked exceptions may thrown before
 * proceed call.  Since around
 * advice can not throw checked exceptions in its signature, the only chance
 * that a proceed may interfere by exceptions is in the try...catch clause, so
 * this analysis only check units in the try...catch
 * clause contains the proceed call.
 * 
 * @author Dehua Zhang
 * 
 */
//TODO we need a special UnitGraph, which will treat throw stmt as normal stmt 
// instead of creating an edge from the throw stmt to the caught stmt
public class ExceptionBeforeProceedAnalysis extends ForwardFlowAnalysis {

	private UnitGraph g;

	private Map<Unit, Set<SootClass>/*checked exceptions been thrown*/> unitsThrownExceptions;
	
	private Set<Unit> unitsInTrap;

	private Unit proceed;

	/**
	 * Return all stmts (Stmt) may throw checked exceptions and thrown
	 * exceptions before proceed. Should be called after 
	 * hasExceptionBefore(Unit).
	 * 
	 * @return all stmts may throw checked exceptions before u, null or empty
	 *         HashMap means no stmts
	 */
	public Map<Unit, Set<SootClass>> getUnitsThrowExceptionsBefore() {
		// no trap, and no analysis
		if (unitsThrownExceptions == null) return null;
		
		FlowSet inSet = (FlowSet)getFlowBefore(proceed);
		// System.out.println("inSet: " + inSet);
		Map<Unit, Set<SootClass>> units = new HashMap<Unit, Set<SootClass>>();
		for (Iterator inIt = inSet.iterator(); inIt.hasNext(); ) {
			Unit u = (Unit)inIt.next();
			units.put(u, unitsThrownExceptions.get(u));
		}
		return units;
	}
	
	public boolean hasExceptionBefore(Unit proceed) {
		
		if (!ImpactUtil.isContainUnit(g.getBody(), proceed)) {
			throw new RuntimeException(
					"The stmt "
							+ proceed
							+ " is not contained in the current unit graph. Creat a new instance of "
							+ this.getClass() + " for the method containing "
							+ proceed + ".");
		}
		
		this.proceed = proceed;
		
		
		List<Trap> traps = TrapManager.getTrapsAt(proceed, g.getBody());
		if (! traps.isEmpty()) {
			// System.out.println("traps: " + traps);
			// add all units in the trap into a set
			unitsInTrap = new HashSet<Unit>();
			for (Trap trap: traps) {
				Chain units = g.getBody().getUnits();
				Iterator<Unit> it = units.iterator(trap.getBeginUnit(), units
						.getPredOf(trap.getEndUnit()));
				while (it.hasNext()) {
					unitsInTrap.add(it.next());
				}
			}

			unitsThrownExceptions = new HashMap<Unit, Set<SootClass>>();
			
			doAnalysis();
			
			FlowSet inSet = (FlowSet)getFlowBefore(proceed);
			return ! inSet.isEmpty();
		} else {
			unitsThrownExceptions = null;
			return false;
		}
	}

	public ExceptionBeforeProceedAnalysis(UnitGraph g) {

		super(g);
		this.g = g;
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
		inSet1.union(inSet2, outSet);
	}

	@Override
	protected void copy(Object source, Object dest) {
		FlowSet srcSet = (FlowSet) source, destSet = (FlowSet) dest;
		srcSet.copy(destSet);
	}

	@Override
	protected Object newInitialFlow() {
		return new ArraySparseSet();
	}

	@Override
	protected Object entryInitialFlow() {
		return new ArraySparseSet();
	}

	@Override
	protected void flowThrough(Object in, Object node, Object out) {

		FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
		inSet.copy(outSet);

		Stmt stmt = (Stmt) node;

		// only care stmt inside the trap
		if (isInSameTrap(stmt)) {
			// System.out.println("==== " + stmt);

			if (stmt instanceof ThrowStmt) {
				ThrowStmt ts = (ThrowStmt) stmt;
				Type exceptionType = ts.getOp().getType();
				if (exceptionType instanceof RefType) {
					SootClass exceptionSootClass = ((RefType) exceptionType)
							.getSootClass();
					if (isCheckedException(exceptionSootClass)) {
						// checked exceptions
						// System.out.println("checked");
						outSet.add(stmt);
						Set<SootClass> exceptions = new HashSet<SootClass>();
						exceptions.add(exceptionSootClass);
						unitsThrownExceptions.put(stmt, exceptions);
					}
				}
			} else if (stmt.containsInvokeExpr()) {
				// for checked exceptions, signature is enough
				boolean gen = false;
				SootMethod st = stmt.getInvokeExpr().getMethod();
				Set<SootClass> exceptions = new HashSet<SootClass>();
				for (Iterator eIt = st.getExceptions().iterator(); eIt
						.hasNext();) {
					SootClass exceptionSootClass = (SootClass) eIt.next();
					if (isCheckedException(exceptionSootClass)) {
						// checked exceptions
						gen = true;
						exceptions.add(exceptionSootClass);
					}
				}
				if (gen) {
					outSet.add(stmt);
					// System.out.println("gen exceptions: " + exceptions);
					unitsThrownExceptions.put(stmt, exceptions);
				}
			}
		}

	}

	private boolean isInSameTrap(Stmt stmt) {
		return unitsInTrap.contains(stmt);
	}

	private boolean isCheckedException(SootClass sc) {
		Hierarchy hierarchy = Scene.v().getActiveHierarchy();
		if (!(hierarchy.isClassSubclassOfIncluding(sc, Scene.v().getSootClass(
				"java.lang.RuntimeException")) || hierarchy
				.isClassSubclassOfIncluding(sc, Scene.v().getSootClass(
						"java.lang.Error")))) {
			return true;
		} else
			return false;
	}
}
