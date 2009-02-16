package abc.impact.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.impact.utils.ImpactUtil;

/**
 * performs the exact proceed call analysis
 * 
 * @author dzhang25
 */
public class ProceedAnalysis extends ForwardFlowAnalysis {

	private Set<Stmt> exactProceedSet;
	
	private Set<Stmt> nonExactProceedSet;
	
	private UnitGraph g;
	
	public static final Integer NONE = new Integer(0);
	public static final Integer NON_EXACT = new Integer(1);
	public static final Integer EXACT = new Integer(2);

	/**
	 * Return stmts containing exact proceed calls 
	 * 
	 * @return 
	 */
	public Set<Stmt> getExactProceeds() {
		return Collections.unmodifiableSet(exactProceedSet);
	}
	
	/**
	 * Return stmts containing proceed calls that are not exact-proceed
	 * @return
	 */
	public Set<Stmt> getNonExactProceeds() {
		return Collections.unmodifiableSet(nonExactProceedSet);
	}

	
	public FlowSet getFlowBeforeEnd() {
		FlowSet inEnd = new ArraySparseSet();
		List<Unit> tails = g.getTails();
		for (Unit u : tails) {
			if (! (u instanceof ThrowStmt)) { //TODO verify this
				inEnd.union((FlowSet)getFlowBefore(u));
			}
		}
		return inEnd;
	}
	
	/**
	 * @param g
	 * @param numFormals
	 *            number of parameters need to be taken into account, since abc
	 *            will generate auxiliary parameters for proceed call, so this
	 *            analysis needs the number of parameters originly declared in 
	 *            around advice
	 * @param withReturn
	 *            if the around adivce has return value
	 */
	public ProceedAnalysis(UnitGraph g, int numFormals, boolean withReturn) {

		super(g);
		this.g = g;

		exactProceedSet = new HashSet<Stmt>();
		nonExactProceedSet = new HashSet<Stmt>();

		UnchangedParamsAnalysis paramAnalysis = null;
		// if numformals = 0, dont need paramAnalysis
		if (numFormals >= 0) paramAnalysis = new UnchangedParamsAnalysis(g, numFormals);
		UnchangedReturnAnalysis returnAnalysis = new UnchangedReturnAnalysis(g);
		ExceptionBeforeProceedAnalysis exceptionAnalysis = new ExceptionBeforeProceedAnalysis(
				g);

		// calcualte exactProceedSet and nonExactProceedSet
		for (Iterator uIt = g.getBody().getUnits().iterator(); uIt.hasNext();) {
			Unit unit = (Unit) uIt.next();
			if (unit instanceof Stmt) {
				Stmt s = (Stmt) unit;
				if (withReturn) {
					// care only AssignStmt
					if (s instanceof AssignStmt
							&& s.containsInvokeExpr()
							&& ImpactUtil.isProceed(((InvokeExpr) s.getInvokeExpr())
									.getMethod())) {
						boolean exact = false;
						int matchArg = numFormals;
						//if numformals = 0, dont need paramAnalysis analysis
						if (numFormals > 0) {
							matchArg = numMatchArgs(numFormals, paramAnalysis, s);
						}
						// System.out.println("invoke match arg = " + matchArg);
						if (matchArg == numFormals) {
							// should check return value and exceptions
							if (returnAnalysis.isReachReturn(s)) {
								if (!exceptionAnalysis.hasExceptionBefore(s)) {
									exact = true;
								}
							}
						}
						if (exact) exactProceedSet.add(s);
						else nonExactProceedSet.add(s);
					}
				} else {
					// care only InvokeStmt
					if (s instanceof InvokeStmt
							&& s.containsInvokeExpr()
							&& ImpactUtil.isProceed(((InvokeExpr) s.getInvokeExpr())
									.getMethod())) {
						boolean exact = false;
						int matchArg = numFormals;
						//if numformals = 0, dont need paramAnalysis analysis
						if (numFormals > 0) {
							matchArg = numMatchArgs(numFormals, paramAnalysis, s);
						}
						// System.out.println("invoke match arg = " + matchArg);
						if (matchArg == numFormals) {
							// should check exceptions
							if (!exceptionAnalysis.hasExceptionBefore(s)) {
								exact = true;
							}
						}
						if (exact) exactProceedSet.add(s);
						else nonExactProceedSet.add(s);
					}
				}
			}
		}

		doAnalysis();
	}

	private int numMatchArgs(int numFormals, UnchangedParamsAnalysis mrpa, Stmt s) {
		FlowSet before = (FlowSet) mrpa.getFlowBefore(s);

		List args = ((InvokeExpr) s.getInvokeExpr()).getArgs();
		int matchArg = 0;
		for (int i = 0; i < numFormals; i++) {
			Value arg = (Value) args.get(i);
			for (Iterator rppIt = before.iterator(); rppIt.hasNext();) {
				IDValuePair rpp = (IDValuePair) rppIt.next();
				if (rpp.groupID == i && arg.equivTo(rpp.value)) {
					matchArg++;
					break;
				}
			}
		}
		return matchArg;
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {

		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
		inSet1.union(inSet2, outSet);

//		 System.out.println("inset1: " + inSet1 + "\ninset2: " + inSet2 + "\noutset: " + outSet);
	}

	@Override
	protected void copy(Object source, Object dest) {
		// copy from source to dest
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
		// perform flow from in to out, through node
		FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
		Unit unit = (Unit) node;

//		 System.out.println(inSet);
//		 System.out.println("=== " + unit);
		// copy inSet to outSet
		inSet.copy(outSet);

		if (unit instanceof Stmt) {
			Stmt s = (Stmt) unit;
			if (exactProceedSet.contains(s)) {
				// exact proceed
				outSet.add(EXACT);
				outSet.remove(NONE);
				outSet.remove(NON_EXACT);
			} else if (nonExactProceedSet.contains(s)) {
				// non-exact proceed
				if (! inSet.contains(EXACT)) {
					outSet.add(NON_EXACT);
					outSet.remove(NONE);
				}
			} else {
				// no proceed
				if (! inSet.contains(NON_EXACT) && ! inSet.contains(EXACT))
					outSet.add(NONE);
			}
		}
//		 System.out.println(outSet);
	}
}



//package abc.impact.analysis;
//
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import soot.Unit;
//import soot.Value;
//import soot.jimple.AssignStmt;
//import soot.jimple.InvokeExpr;
//import soot.jimple.InvokeStmt;
//import soot.jimple.Stmt;
//import soot.toolkits.graph.UnitGraph;
//import soot.toolkits.scalar.ArraySparseSet;
//import soot.toolkits.scalar.FlowSet;
//import soot.toolkits.scalar.ForwardFlowAnalysis;
//import abc.impact.utils.ImpactUtil;
//
///**
// * performs the exact proceed call analysis
// * 
// * @author dzhang25
// */
//// TODO change Unit to Stmt
//public class NumOfProceedAnalysis extends ForwardFlowAnalysis {
//	private ArraySparseSet initialSet;
//
//	private ArraySparseSet fullSet;
//
//	private HashSet<Stmt> exactProceedSet;
//
//	private boolean hasExactProceedEveryPath;
//	private boolean onlyOneExactProceedAllPath;
//
//	/**
//	 * Return exact proceed calls (if a proceed call is passed in the origin
//	 * arguments of the around advice and its return value, if applicalbe, is
//	 * returned by around adivce without modification, it is call exact proceed
//	 * call.) on every path of the control flow
//	 * 
//	 * @return HashSet contains exact proceed calls
//	 */
//	public Set<Stmt> getProceeds() {
//		return Collections.unmodifiableSet(exactProceedSet);
//	}
//
//	public boolean hasExactProceed() {
//		return !exactProceedSet.isEmpty();
//	}
//	
//	public boolean hasExactProceedEveryPath() {
//		return hasExactProceedEveryPath;
//	}
//
//	public boolean isOnlyOneExactProceedAllPath() {
//		return onlyOneExactProceedAllPath;
//	}
//	
//	/**
//	 * @param g
//	 * @param numFormals
//	 *            number of parameters need to be taken into account, since abc
//	 *            will generate auxiliary parameters for proceed call, so this
//	 *            analysis needs the number of parameters originly declared in 
//	 *            around advice
//	 * @param withReturn
//	 *            if the around adivce has return value
//	 */
//	public NumOfProceedAnalysis(UnitGraph g, int numFormals, boolean withReturn) {
//
//		super(g);
//
//		initialSet = new ArraySparseSet();
//		fullSet = new ArraySparseSet();
//		exactProceedSet = new HashSet<Stmt>();
//		hasExactProceedEveryPath = true;
//		onlyOneExactProceedAllPath = true;
//
//		UnchangedParamsAnalysis paramAnalysis = null;
//		// if numformals = 0, dont need paramAnalysis analysis
//		if (numFormals >= 0) paramAnalysis = new UnchangedParamsAnalysis(g, numFormals);
//		UnchangedReturnAnalysis returnAnalysis = new UnchangedReturnAnalysis(g);
//		ExceptionBeforeProceedAnalysis exceptionAnalysis = new ExceptionBeforeProceedAnalysis(
//				g);
//
//		// calcualte exactProceedSet and the full set
//		for (Iterator uIt = g.getBody().getUnits().iterator(); uIt.hasNext();) {
//			Unit unit = (Unit) uIt.next();
//			if (unit instanceof Stmt) {
//				Stmt s = (Stmt) unit;
//				if (withReturn) {
//					// care only AssignStmt
//					if (s instanceof AssignStmt
//							&& s.containsInvokeExpr()
//							&& ImpactUtil.isProceed(((InvokeExpr) s.getInvokeExpr())
//									.getMethod())) {
//						int matchArg = numFormals;
//						//if numformals = 0, dont need paramAnalysis analysis
//						if (numFormals > 0) {
//							matchArg = numMatchArgs(numFormals, paramAnalysis, s);
//						}
//						// System.out.println("invoke match arg = " + matchArg);
//						if (matchArg == numFormals) {
//							// should check return value and exceptions
//							if (returnAnalysis.isReachReturn(s)) {
//								if (!exceptionAnalysis.hasExceptionBefore(s)) {
//									exactProceedSet.add(s);
//									fullSet.add(s);
//								}
//							}
//						}
//					}
//				} else {
//					// care only InvokeStmt
//					if (s instanceof InvokeStmt
//							&& s.containsInvokeExpr()
//							&& ImpactUtil.isProceed(((InvokeExpr) s.getInvokeExpr())
//									.getMethod())) {
//						int matchArg = numFormals;
//						//if numformals = 0, dont need paramAnalysis analysis
//						if (numFormals > 0) {
//							matchArg = numMatchArgs(numFormals, paramAnalysis, s);
//						}
////						System.out.println("invoke match arg = " + matchArg);
//						if (matchArg == numFormals) {
//							// should check exceptions
//							if (!exceptionAnalysis.hasExceptionBefore(s)) {
//								exactProceedSet.add(s);
//								fullSet.add(s);
//							}
//						}
//					}
//				}
//			}
//		}
//
//		// System.out.println("intialSet and fullSet");
//		// System.out.println(initialSet);
//		// System.out.println(fullSet);
//
//		doAnalysis();
//	}
//
//	private int numMatchArgs(int numFormals, UnchangedParamsAnalysis mrpa, Stmt s) {
//		FlowSet before = (FlowSet) mrpa.getFlowBefore(s);
//
//		List args = ((InvokeExpr) s.getInvokeExpr()).getArgs();
//		int matchArg = 0;
//		for (int i = 0; i < numFormals; i++) {
//			Value arg = (Value) args.get(i);
//			for (Iterator rppIt = before.iterator(); rppIt.hasNext();) {
//				IDValuePair rpp = (IDValuePair) rppIt.next();
//				if (rpp.groupID == i && arg.equivTo(rpp.value)) {
//					matchArg++;
//					break;
//				}
//			}
//		}
//		return matchArg;
//	}
//
//	// @Override
//	protected void merge(Object in1, Object in2, Object out) {
//
//		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
//		outSet.clear();
//		// at lease one proceed on each branch, merge them(take the small inSet as outSet);
//		// otherwise, set outSet empty (done before)
//		if (inSet1.size() >= 1 && inSet2.size() >= 1) {
//			if (inSet1.size() < inSet2.size()) {
//				inSet1.copy(outSet);
//			} else 
//				inSet2.copy(outSet);
//		} else {
//			hasExactProceedEveryPath = false;
//		}
//		
//		// if not exact one proceed on each branch, set the flag false
//		if (inSet1.size() != 1 && inSet2.size() != 1) {
//			onlyOneExactProceedAllPath = false;
//		}
//
//		// System.out.println("inset1: " + inSet1 + "\ninset2: " + inSet2 +
//		// "\noutset: " + outSet);
//	}
//
//	// @Override
//	protected void copy(Object source, Object dest) {
//		// copy from source to dest
//		FlowSet srcSet = (FlowSet) source, destSet = (FlowSet) dest;
//		srcSet.copy(destSet);
//	}
//
//	// @Override
//	protected Object newInitialFlow() {
//		// return the full set
//		return fullSet.clone();
//	}
//
//	// @Override
//	protected Object entryInitialFlow() {
//		// return the initial set
//		return initialSet.clone();
//	}
//
//	// @Override
//	protected void flowThrough(Object in, Object node, Object out) {
//		// perform flow from in to out, through node
//		FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
//		Unit unit = (Unit) node;
//
//		// copy inSet to outSet
//		inSet.copy(outSet);
//
//		// if contains proceed invokation and is an exactProceed, gen
//		if (unit instanceof Stmt) {
//			Stmt s = (Stmt) unit;
//			if (s.containsInvokeExpr()) {
//				if (ImpactUtil.isProceed(s.getInvokeExpr().getMethod())
//						&& exactProceedSet.contains(s))
//					outSet.add(s);
//			}
//		}
//		// no kill here, kill when merging
//
//		// more than one exactProceed found
//		if (outSet.size() > 1)
//			hasExactProceedEveryPath = false;
//	}
//
//
//	// private class ProceedFlowSet extends ArraySparseSet
//	// {
//	// private HashSet exactProceedSet;
//	//		
//	// public ProceedFlowSet()
//	// {
//	// super();
//	// exactProceedSet = new HashSet();
//	// }
//	//		
//	// public void addExactProceed(Unit u) {
//	// exactProceedSet.add(u);
//	// }
//	//		
//	// public void clear() {
//	// super.clear();
//	// exactProceedSet.clear();
//	// }
//	//		
//	// public void copy() {
//	// super.copy
//	// }
//	// }
//
//	// /**
//	// * @author Dehua Zhang
//	// *
//	// * Wrap the proceed call Unit
//	// */
//	// private class ProceedContainer {
//	//
//	// private ArrayList proceedList;
//	//
//	// public ProceedContainer() {
//	// proceedList = new ArrayList();
//	// }
//	//
//	// public ProceedContainer(Unit u) {
//	// proceedList = new ArrayList();
//	// this.add(u);
//	// }
//	//
//	// public void add(Unit u) {
//	// proceedList.add(u);
//	// }
//	//		
//	// /**
//	// * merge this and other into dest
//	// */
//	// public void merge(ProceedContainer other, ProceedContainer dest)
//	// {
//	// dest.proceedList.addAll(this.proceedList);
//	// dest.proceedList.addAll(other.proceedList);
//	// }
//	//
//	// public int getCount() {
//	// return proceedList.size();
//	// }
//	//
//	// public int hashCode() {
//	// final int PRIME = 31;
//	// int result = 1;
//	// result = PRIME * result;
//	// return result;
//	// }
//	//
//	// public boolean equals(Object obj) {
//	// if (this == obj)
//	// return true;
//	// if (obj == null)
//	// return false;
//	// if (getClass() != obj.getClass())
//	// return false;
//	// final ProceedContainer other = (ProceedContainer) obj;
//	// if (proceedList == null) {
//	// if (other.proceedList != null)
//	// return false;
//	// } else if (proceedList.size() != other.proceedList.size()) {
//	// return false;
//	// } else {
//	// for (Iterator plIt = proceedList.iterator(); plIt.hasNext(); ) {
//	// if (! other.proceedList.contains(plIt.next())) {
//	// return false;
//	// }
//	// }
//	// }
//	// return true;
//	// }
//	// }
//
//}
