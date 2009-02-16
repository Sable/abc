package abc.impact.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.G;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.pointer.MemoryEfficientRasUnion;
import soot.jimple.toolkits.pointer.PASideEffectTester;
import soot.jimple.toolkits.pointer.Union;
import soot.jimple.toolkits.pointer.UnionFactory;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import abc.impact.utils.ImpactUtil;

/**
 * @author Dehua Zhang
 * 
 * given a stmt, this analysis test if the left of the stmt reaches all returns
 */
public class UnchangedReturnAnalysis extends BackwardFlowAnalysis {

	private ArraySparseSet initialSet;

	private UnitGraph g;

	private PASideEffectTester past;
	
	private PointsToAnalysis pa;
	
	private HashMap<Value, List<FieldRef>> fieldRefCache;
	
	/**
	 * a value which would never appear in real world
	 */
	private final Value STUB_VALUE = Jimple.v().newMulExpr(NullConstant.v(), NullConstant.v());	
//	private Unit proceed;
//
//	private boolean reachReturn;

//	public boolean isReachReturn() {
//		return reachReturn;
//	}
	
	public boolean isReachReturn(Unit proceed) {
		
		if (!ImpactUtil.isContainUnit(g.getBody(), proceed)) {
			throw new IllegalArgumentException(
					"The stmt "
							+ proceed
							+ " is not contained in the current unit graph. Creat a new instance of "
							+ this.getClass() + " for the method containing "
							+ proceed + ".");
		}

		boolean reachReturn = true;

		if (proceed instanceof AssignStmt) {
	
			//test if every value in outSet of proceed equals to left of proceed
			FlowSet outSet = (FlowSet)getFlowAfter(proceed);
			
			Value retOfProceed = ((AssignStmt)(proceed)).getLeftOp();
			//System.out.println("outSet of proceed" + outSet);

			for (Iterator outIt = outSet.iterator(); outIt.hasNext();) {
				Value value = (Value) outIt.next();
				if (!value.equivTo(retOfProceed))
					reachReturn = false;
			}
		} else {
			// since no return value of this proceed call is collected
			reachReturn = false;
		}
		
		return reachReturn;
	}

//	public void newProceed(Unit proceed) {
//
//		if (!Tool.isContainUnit(g.getBody(), proceed)) {
//			throw new RuntimeException(
//					"The stmt "
//							+ proceed
//							+ " is not contained in the current unit graph. Creat a new instance of "
//							+ this.getClass() + " for the method containing "
//							+ proceed + ".");
//		}
//		this.proceed = proceed;
//		reachReturn = true;
//
//		if (this.proceed instanceof AssignStmt) {
//			doAnalysis();
//			
//			//then test if every value in outSet of proceed equals to left of proceed
//			FlowSet outSet = (FlowSet)getFlowAfter(this.proceed);
//			
//			Value retOfProceed = ((AssignStmt)(this.proceed)).getLeftOp();
//			System.out.println("outSet of proceed" + outSet);
//
//			for (Iterator outIt = outSet.iterator(); outIt.hasNext();) {
//				Value value = (Value) outIt.next();
//				if (!value.equivTo(retOfProceed))
//					reachReturn = false;
//			}
//		} else {
//			// since no return value of this proceed call is collected
//			reachReturn = false;
//		}
//	}

	public UnchangedReturnAnalysis(UnitGraph g) {

		super(g);
		this.g = g;

		G.v().Union_factory = new UnionFactory() {
		    public Union newUnion() { return new MemoryEfficientRasUnion(); }
		};
		
		past = new PASideEffectTester();
		past.newMethod(g.getBody().getMethod());
		pa = Scene.v().getPointsToAnalysis();
		fieldRefCache = new HashMap<Value, List<FieldRef>>();
		
		initialSet = new ArraySparseSet();
		doAnalysis();
	}

	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
		inSet1.union(inSet2, outSet);
	}

	// @Override
	protected void copy(Object source, Object dest) {
		// copy from source to dest
		FlowSet srcSet = (FlowSet) source, destSet = (FlowSet) dest;
		srcSet.copy(destSet);
	}

	// @Override
	protected Object newInitialFlow() {
		// return the full set
		return new ArraySparseSet();
	}

	// @Override
	protected Object entryInitialFlow() {
		// return the initial set
		return initialSet.clone();
	}

	// @Override
	protected void flowThrough(Object in, Object node, Object out) {

		// perform flow from out to in, through node
		FlowSet outSet = (FlowSet) in, inSet = (FlowSet) out;
		Unit unit = (Unit) node;

		// copy outSet to inSet
		outSet.copy(inSet);

		// a. if the stmt is assign stmt,
		//    inSet = outSet union genSet - killSet
		//    gen right and kill left if left in set;
		//    kill left.base and gen stub
		// b. if contains invokeExp
		//    kill sideeffect and gen stub
		// c. if return stmt, gen return op
		// d. otherwise, inSet = outSet
		// The stub is necessary and should be a value which would never appera in real world,
		// which is used to represent the value that is returned by a branch but is impossible
		// returned by the proceed; otherwise, incorrect analysis result
		// ex:
		//   a = proceed(...); in={stub, a}
		//   if (x > y) {
		//     a.x = ...; //kill base, which is a; in={stub}
		//     change(a); //kill a; in={stub}
		//     return a; in={a}
		//   } else {
		//     return a; in={a}
		//   }
		if (unit instanceof AssignStmt) {

			AssignStmt as = (AssignStmt) unit;
			Value left = as.getLeftOp(), right = as.getRightOp();
			Value leftBase = null;
			if (left instanceof InstanceFieldRef) {
				leftBase = ((InstanceFieldRef) left).getBase();
			}

			if (outSet.contains(left)) {
				inSet.add(right);
				inSet.remove(left);
				killAlias(inSet, left);
			}

			if (leftBase != null && inSet.contains(leftBase)) {
				//System.out.println("************* kill left base" + leftBase);
				inSet.remove(leftBase);
				killAlias(inSet, leftBase);
				inSet.add(STUB_VALUE);
			}
		}
		
		if (unit instanceof Stmt && ((Stmt)unit).containsInvokeExpr()) {
			Stmt stmt = (Stmt)unit;
			//System.out.println("******************" + unit);
			FlowSet tempSet = (FlowSet)inSet.clone();

			for (Iterator tempIt = tempSet.iterator(); tempIt.hasNext(); ) {
				Value v = (Value) tempIt.next();
				if (v instanceof Expr) continue;
				
				Type vType = v.getType();
				boolean immutable = false;
				
				if (vType instanceof RefType) {
					String vClass = "";
					vClass = ((RefType)vType).getClassName();
					//it seems the side effect tester can not deal with String correctly
					if (vClass.equals("java.lang.String"))
						immutable = true;
				}
				
				// write to value itself
				if (past.unitCanWriteTo(stmt, v)) {
					//System.out.println("write yy" + v);
					inSet.remove(v);
					killAlias(inSet, v);
					inSet.add(STUB_VALUE);
					continue;
				}
				
				if (!immutable) {
					// write to fields of value
					for (Iterator<FieldRef> frIt = getFieldRefsIn(v).iterator(); frIt.hasNext(); ) {
						FieldRef fr = frIt.next();
						//System.out.println("fieldRef: " + fr);
						if (past.unitCanWriteTo(stmt, fr)) {
							//System.out.println("write yy" + fr);
							//kill current value
							inSet.remove(v);
							killAlias(inSet, v);
							inSet.add(STUB_VALUE);
							break;
						}
					}
				}
			}
		}
		
		// do not need to consider return; without ret value
		// it is not possible, compile-error.
		if (unit instanceof ReturnStmt) {
			Value ret = ((ReturnStmt) unit).getOp();
			inSet.add(ret);
		}
	}
	
	/**
	 * return a list of FieldRef of all fields of the SootClass if v is a RefType
	 * ex: Object v; //return all FieldRef of all fields of Object 
	 * @param v a value
	 * @return a list of FieldRef or empty list if v is not RefType and not JimpleLocal
	 */
	private List<FieldRef> getFieldRefsIn (Value v) {
		
		List<FieldRef> ret;
		
		ret = fieldRefCache.get(v);
		if (ret != null) {
			//System.out.println("cache hit");
			return ret;
		}
		
		if (v instanceof JimpleLocal && v.getType() instanceof RefType) {
			ret = new LinkedList<FieldRef>();
			Value base = v;
			//System.out.println(base);
			SootClass sc = ((RefType)v.getType()).getSootClass();
			for (Iterator<SootField> sfIt = sc.getFields().iterator(); sfIt.hasNext();) {

				SootField sf = sfIt.next();
				SootFieldRef sfr = sf.makeRef();
				FieldRef fieldRef = null;
				if (sf.isStatic()) {
					fieldRef = Jimple.v().newStaticFieldRef(sfr);
				} else {
					fieldRef = Jimple.v().newInstanceFieldRef(base, sfr);
				}
				ret.add(fieldRef);
				//System.out.println("field: " + sf + " fieldRef: " + fieldRef);
			}
			
			ret = Collections.unmodifiableList(ret);
		} else {
			ret = Collections.EMPTY_LIST;
		}
		fieldRefCache.put(v, ret);
		
		return ret;
	}
	
	/**
	 * kill due to alias, kill all alias of pair
	 * @param s the flowset
	 * @param v the pair being killed
	 */
	private void killAlias(FlowSet s, Value v) {
		if (! (v instanceof Local)) return;
		
		PointsToSet pairPTSet = pa.reachingObjects((Local)v);
		FlowSet tempSet = (FlowSet)s.clone();
		
		for (Iterator tempIt = tempSet.iterator(); tempIt.hasNext();) {
			Value fv = (Value) tempIt.next();
			if (fv instanceof Local) {
				PointsToSet rppPTSet = pa.reachingObjects((Local)fv);
				if (rppPTSet.hasNonEmptyIntersection(pairPTSet)) {
					//kill
					s.remove(fv);
				}
			}
		}
	}
}

// import java.util.Iterator;
//
//import soot.EquivalentValue;
//import soot.Unit;
//import soot.Value;
//import soot.ValueBox;
//import soot.jimple.AssignStmt;
//import soot.jimple.IfStmt;
//import soot.jimple.LookupSwitchStmt;
//import soot.jimple.ReturnStmt;
//import soot.jimple.TableSwitchStmt;
//import soot.jimple.ThrowStmt;
//import soot.toolkits.graph.UnitGraph;
//import soot.toolkits.scalar.ArraySparseSet;
//import soot.toolkits.scalar.FlowSet;
//import soot.toolkits.scalar.ForwardFlowAnalysis;
//import abc.impact.tools.Tool;
//
///**
// * @author Dehua Zhang
// * 
// * given a stmt, this analysis analyze all variables that appears after this
// * stmt and have the same value of the def value of this stmt
// */
//public class UnchangedReturnAnalysis extends ForwardFlowAnalysis {
//
//	private ArraySparseSet initialSet;
//
//	private ArraySparseSet fullSet;
//
//	private UnitGraph g;
//
//	private Unit proceed;
//
//	private boolean startFlow;
//
//	private int seenBranch;
//
//	private boolean reachReturn;
//
////	private boolean exceptionBefore;
//
////	private boolean doReturnAnalysis;
//
//	public boolean isReachReturn() {
//		return reachReturn;
//	}
//
////	public boolean hasExceptionBefore() {
////		return exceptionBefore;
////	}
//
//	public void newProceed(Unit proceed) {
//
//		if (! Tool.isContainUnit(g.getBody(), proceed)) {
//			throw new RuntimeException(
//					"The stmt "
//							+ proceed
//							+ " is not contained in the current unit graph. Creat a new instance of "
//							+ this.getClass() + " for the method containing "
//							+ proceed + ".");
//		}
//		this.proceed = proceed;
//		reachReturn = true;
////		exceptionBefore = false;
//		startFlow = false;
//		seenBranch = 0;
//
//		if (proceed instanceof AssignStmt) {
////			doReturnAnalysis = true;
//			doAnalysis();
//		} else {
//			reachReturn = false; // since no return value of this proceed
//									// call
////			doReturnAnalysis = false;
//		}
//
////		doAnalysis();
//	}
//
//	public UnchangedReturnAnalysis(UnitGraph g) {
//
//		super(g);
//		this.g = g;
//
//		initialSet = new ArraySparseSet();
//		fullSet = new ArraySparseSet();
//
//		// calculate the full set
//		for (Iterator uIt = g.getBody().getUnits().iterator(); uIt.hasNext();) {
//			Unit u = (Unit) uIt.next();
//
//			if (u instanceof AssignStmt) {
//				AssignStmt as = (AssignStmt) u;
//				for (Iterator defBoxIt = as.getDefBoxes().iterator(); defBoxIt
//						.hasNext();) {
//					Value def = ((ValueBox) defBoxIt.next()).getValue();
//					fullSet.add(new EquivalentValue(def));
//				}
//				// TODO AssignStmt contains InvokeExpr
//			}
//			// TODO invokestmt all RefLikeType params of the invoked method
//			// should be inserted into fullSet
//		}
//
//		// System.out.println("intialSet and fullSet");
//		// System.out.println(initialSet);
//		// System.out.println(fullSet);
//	}
//
//	// @Override
//	protected void merge(Object in1, Object in2, Object out) {
////		if (doReturnAnalysis) {
//			FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
//			if (seenBranch > 0) {
//				inSet1.intersection(inSet2, outSet);
//				seenBranch--;
////				System.out.println("merger intersection @@@@" + seenBranch);
//			} else {
//				inSet1.union(inSet2, outSet);
////				System.out.println("merger union @@@@" + seenBranch);
//			}
////		}
//	}
//
//	// @Override
//	protected void copy(Object source, Object dest) {
////		if (doReturnAnalysis) {
//			// copy from source to dest
//			FlowSet srcSet = (FlowSet) source, destSet = (FlowSet) dest;
//			srcSet.copy(destSet);
////		}
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
//
//		// perform flow from in to out, through node
//		FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
//		Unit unit = (Unit) node;
////		System.out.println("retret ==== " + unit);
//
////		if (doReturnAnalysis) {
//			// copy inSet to outSet
//			inSet.copy(outSet);
//
//			if (startFlow) {
//				if (unit instanceof IfStmt) {
//					seenBranch++;
////					 System.out.println("seen if branch: " + seenBranch);
//				}
//
//				if (unit instanceof TableSwitchStmt) {
//					seenBranch += ((TableSwitchStmt) unit).getTargets().size();
////					 System.out.println("seen table branch: " + seenBranch);
//				}
//
//				if (unit instanceof LookupSwitchStmt) {
//					seenBranch += ((LookupSwitchStmt) unit).getTargetCount();
////					 System.out.println("seen lookup branch: " + seenBranch);
//				}
//				// if the stmt is assign stmt, calcualte outSet = (inSet(outSet
//				// now
//				// contains the copy of inSet) - killSet) union genSet;
//				// otherwise,
//				// outSet = inSet (already done above)
//				if (unit instanceof AssignStmt) {
//
//					AssignStmt as = (AssignStmt) unit;
//					Value left = as.getLeftOp(), right = as.getRightOp();
//
//					// kill
//					boolean generable = false, killable = false;
//					;
//					for (Iterator outIt = outSet.iterator(); outIt.hasNext();) {
//						EquivalentValue v = ((EquivalentValue) outIt.next());
//						if (v.equivToValue(right))
//							generable = true;
//						if (v.equivToValue(left))
//							killable = true;
//					}
//
//					if (killable != generable) {
//						// kill
//						if (killable)
//							outSet.remove(new EquivalentValue(left));
//						// gen
//						if (generable)
//							outSet.add(new EquivalentValue(left));
//					}
//					// TODO change leftOp to defBoxes??
//					// TODO def/leftOp is InstantFieldRef, but soot will not
//					// treat the object as def,
//					// however, here we need to treat is as changed
//					// InstantFieldRef.getBase
//					// System.out.println("in set " + inSet + " out set " +
//					// outSet);
//				}
//
//				// TODO if unit is invoke stmt and params are RefLikeType
//				// go inside check if RefLikeType params changed in the invoked
//				// method
//				// recursively
//				// if yes, need to kill it.
//
//				if (unit instanceof ReturnStmt) {
//					// if the return value of the ReturnStmt is not contained in
//					// inSet, this return do not have the save value returned by
//					// proceed
//					ReturnStmt rs = (ReturnStmt) unit;
//					if (!inSet.contains(new EquivalentValue(rs.getOp()))) {
//						// System.out.println("return not included " + inSet + "
//						// " + rs.getOp());
//						reachReturn = false;
//					}
//				}
//			}
////		}
//
//		if (unit.equals(proceed)) {
////			if (doReturnAnalysis) {
//				for (Iterator defIt = proceed.getDefBoxes().iterator(); defIt
//						.hasNext();) {
//					EquivalentValue eqvalue = new EquivalentValue(
//							((ValueBox) defIt.next()).getValue());
//					outSet.add(eqvalue);
//				}
////			}
//			startFlow = true;
//			// System.out.println("start flow " + outSet);
//		}
//
//		if (!startFlow) {
//			// if having return before startFlow, this return can not have the
//			// save value returned by proceed
//			// **there should not have ReturnVoidStmt, the NumOfProceedAnalysis
//			// ensure this
////			if (doReturnAnalysis) {
//				if (unit instanceof ReturnStmt) {
//					// System.out.println("return found before start");
//					reachReturn = false;
//				}
////			}
////			if (unit instanceof ThrowStmt) {
////				exceptionBefore = true;
////			}
//		}
//	}
//}
