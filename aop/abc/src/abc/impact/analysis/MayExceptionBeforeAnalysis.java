//package abc.impact.analysis;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//
//import soot.Hierarchy;
//import soot.RefType;
//import soot.Scene;
//import soot.SootClass;
//import soot.SootMethod;
//import soot.Trap;
//import soot.Type;
//import soot.Unit;
//import soot.jimple.IdentityStmt;
//import soot.jimple.Stmt;
//import soot.jimple.ThrowStmt;
//import soot.toolkits.graph.UnitGraph;
//import soot.toolkits.scalar.ArraySparseSet;
//import soot.toolkits.scalar.FlowSet;
//import soot.toolkits.scalar.ForwardFlowAnalysis;
//import soot.util.Chain;
//import abc.impact.tools.Tool;
//
//public class MayExceptionBeforeAnalysis extends ForwardFlowAnalysis {
//
//	private UnitGraph g;
//	private HashMap unitThrownExceptions; // (unit, HashSet(SootClass)) cache exceptions thrown by stmt
//
//	/**
//	 * Return all stmts (Stmt) may throw checked exceptions before u
//	 * @param u Stmt to be tested
//	 * @return all stmts may throw checked exceptions before u
//	 */
//	public FlowSet getUnitsThrowExceptionsBefore(Unit u) {
//		if (!Tool.isContainUnit(g.getBody(), u)) {
//			throw new RuntimeException(
//					"The stmt "
//							+ u
//							+ " is not contained in the current unit graph. Creat a new instance of "
//							+ this.getClass() + " for the method containing "
//							+ u + ".");
//		}
//
//		return (FlowSet) this.getFlowBefore(u);
//	}
//
//	/**
//	 * Return if there are stmts (Stmt) may throw checked exceptions before u
//	 * @param u Stmt to be tested
//	 * @return if there are stmts (Stmt) may throw checked exceptions before u
//	 */
//	public boolean hasExceptionBefore(Unit u) {
//		if (!Tool.isContainUnit(g.getBody(), u)) {
//			throw new RuntimeException(
//					"The stmt "
//							+ u
//							+ " is not contained in the current unit graph. Creat a new instance of "
//							+ this.getClass() + " for the method containing "
//							+ u + ".");
//		}
//
//		return !((FlowSet) this.getFlowBefore(u)).isEmpty();
//	}
//
//	public MayExceptionBeforeAnalysis(UnitGraph g) {
//
//		super(g);
//		this.g = g;
//		unitThrownExceptions = new HashMap();
//		doAnalysis();
//	}
//
//	// @Override
//	protected void merge(Object in1, Object in2, Object out) {
//		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
//		inSet1.union(inSet2, outSet);
//	}
//
//	// @Override
//	protected void copy(Object source, Object dest) {
//		FlowSet srcSet = (FlowSet) source, destSet = (FlowSet) dest;
//		srcSet.copy(destSet);
//	}
//
//	// @Override
//	protected Object newInitialFlow() {
//		return new ArraySparseSet();
//	}
//
//	// @Override
//	protected Object entryInitialFlow() {
//		// return the initial set
//		return new ArraySparseSet();
//	}
//
//	// @Override
//	protected void flowThrough(Object in, Object node, Object out) {
//
//		FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
//		inSet.copy(outSet);
//
//		Stmt stmt = (Stmt) node;
//		System.out.println("==== " + stmt);
//		System.out.println("in " + outSet);
//
//		if (stmt instanceof ThrowStmt) {
//			ThrowStmt ts = (ThrowStmt) stmt;
//			Type exceptionType = ts.getOp().getType();
//			if (exceptionType instanceof RefType) {
//				SootClass exceptionSootClass = ((RefType) exceptionType)
//						.getSootClass();
//				Hierarchy hierarchy = Scene.v().getActiveHierarchy();
//				if (isCheckedException(exceptionSootClass)) {
//					// check exceptions
////					System.out.println("checked");
//					outSet.add(stmt); //gen
//					unitThrownExceptions.put(stmt, (new HashSet()).add(exceptionSootClass));
//				}
//			}
//		} else if (stmt.containsInvokeExpr()) {
//			// for checked exceptions, signature is enough
//			boolean genable = false;
//			SootMethod st = stmt.getInvokeExpr().getMethod();
//			HashSet exceptions = null;
//			for (Iterator eIt = st.getExceptions().iterator(); eIt.hasNext();) {
//				SootClass exceptionSootClass = (SootClass) eIt.next();
//				if (isCheckedException(exceptionSootClass)) {
//					// check exceptions
//					genable = true;
//					if (exceptions == null) exceptions = new HashSet();
//					exceptions.add(exceptionSootClass);
//				}
//			}
//			if (genable) {
//				outSet.add(stmt); //gen
//				System.out.println("gen exceptions: " + exceptions);
//				unitThrownExceptions.put(stmt, exceptions);
//			}
//		} else if (stmt instanceof IdentityStmt) {
//			// As far as I know, handlers are all JIdentityStmt
//			Trap trap = handledTrap(stmt);
//			if (trap != null) { //current stmt is a handler
//				
//				//for each stmt in current flowset
//				for (Iterator outIt = outSet.iterator(); outIt.hasNext(); ) {
//					Stmt stmtInOutSet = (Stmt) outIt.next();
//					boolean inTrap = false;
//					//1. check if stmtInOutSet in the try block of the current handler
//					//TODO put it in TrapManager isUnitInTrap(Unit u, Trap t); and in Trap getUnits(); 
//					Chain units = g.getBody().getUnits();
//		            Iterator it = units.iterator(trap.getBeginUnit(), units.getPredOf(trap.getEndUnit()));
//		            while (it.hasNext()) {
//		            	if (((Unit)it.next()).equals(stmtInOutSet)) {
//		            		inTrap = true;
//		            		System.out.println("in trap " + stmtInOutSet);
//		            		break;
//		            	}
//		            }
//		            
//		            // 2. check if all exceptions thrown by stmtInOutSet are caught (probably not just by this handler, remember a try block may have several handler)
//		            if (inTrap) {
//						HashSet exceptions = (HashSet) unitThrownExceptions.get(stmtInOutSet);
//						if (exceptions == null) {
//							// exceptions have been caught by previous handlers (it is possible, since we only cares about some types of exceptions)
//							outSet.remove(stmtInOutSet); //kill
//						} else {
//							SootClass caughtException = trap.getException();
//							System.out.println("exceptions thrown: " + exceptions);
//							//for each exception thrown by stmtInOutSet
//							for (Iterator exceptionIt = exceptions.iterator(); exceptionIt.hasNext(); ) {
//								//if one exception is not caught, not killable
//								SootClass thrownException = (SootClass) exceptionIt.next();
//								if (Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(thrownException, caughtException)) {
//									exceptions.remove(thrownException);
//								}
//							}
//							System.out.println("exceptions thrown: " + exceptions);
//							if (exceptions.isEmpty()) {
//								outSet.remove(stmtInOutSet); //kill
//								unitThrownExceptions.remove(stmtInOutSet); //remove from cache
//							}
//						}
//		            }
//				}
//			}
//		}
//		
//		System.out.println("out: " + outSet);
//	}
//
//	private boolean isCheckedException(SootClass sc) {
//		Hierarchy hierarchy = Scene.v().getActiveHierarchy();
//		if (!(hierarchy.isClassSubclassOfIncluding(sc, Scene.v().getSootClass(
//				"java.lang.RuntimeException")) || hierarchy
//				.isClassSubclassOfIncluding(sc, Scene.v().getSootClass(
//						"java.lang.Error")))) {
//			return true;
//		} else
//			return false;
//	}
//	
//	private Trap handledTrap (Unit u) {
//		for (Iterator trapIt = g.getBody().getTraps().iterator(); trapIt.hasNext(); ) {
//			Trap trap = (Trap) trapIt.next();
//			if (trap.getHandlerUnit().equals(u)) {
//				return trap;
//			}
//		}
//		return null;
//	}
//}
