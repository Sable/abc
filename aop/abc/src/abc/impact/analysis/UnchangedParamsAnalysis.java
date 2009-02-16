package abc.impact.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.G;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.pointer.MemoryEfficientRasUnion;
import soot.jimple.toolkits.pointer.PASideEffectTester;
import soot.jimple.toolkits.pointer.Union;
import soot.jimple.toolkits.pointer.UnionFactory;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * performs the must reach params analysis, which record all variables that has
 * the same value as params
 * 
 * @author Dehua Zhang
 */
public class UnchangedParamsAnalysis extends ForwardFlowAnalysis {
	private ArraySparseSet initialSet;

	private ArraySparseSet fullSet;
	
	private PASideEffectTester past;
	
	private PointsToAnalysis pa;
	
	private HashMap<Value, List<FieldRef>> fieldRefCache;

	/**
	 * @param g
	 * @param numFormals
	 *            number of formals declared in the advice declaration. Abc
	 *            appends internal used parameters for around advice, so this
	 *            analysis needs the actual number of formals in advice
	 *            declaration.
	 */
	public UnchangedParamsAnalysis(UnitGraph g, int numFormals) {

		super(g);

		// TODO to improve the performance, create own flowset use the
		// following (key, value) pair
		// (formalIndex, (list of local vars has the same value as params))
		G.v().Union_factory = new UnionFactory() {
		    public Union newUnion() { return new MemoryEfficientRasUnion(); }
		};
		
		past = new PASideEffectTester();
		past.newMethod(g.getBody().getMethod());
		pa = Scene.v().getPointsToAnalysis();
		fieldRefCache = new HashMap<Value, List<FieldRef>>();

		initialSet = new ArraySparseSet();
		fullSet = new ArraySparseSet();

		// calculate the initial set and full set
		for (int i = 0; i < numFormals; i++) {
			Local local = g.getBody().getParameterLocal(i);
			// System.out.println("parameter local " + i + ": " + local);
			initialSet.add(new IDValuePair(i, local));
			fullSet.add(new IDValuePair(i, local));
		}

		// calcualte the full set
		for (Iterator uIt = g.getBody().getUnits().iterator(); uIt.hasNext();) {
			Unit u = (Unit) uIt.next();

			if (u instanceof AssignStmt) {
				AssignStmt as = (AssignStmt) u;
				Value left = as.getLeftOp();
				for (int i = 0; i < numFormals; i++) {
					fullSet.add(new IDValuePair(i, left));
				}
			}
		}

		// System.out.println("intialSet and fullSet");
		// System.out.println(initialSet);
		// System.out.println(fullSet);
		doAnalysis();
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
		// must analysis => out = in1 intersection in2
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
		inSet1.intersection(inSet2, outSet);

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
		// return the full set
		return fullSet.clone();
	}

	@Override
	protected Object entryInitialFlow() {
		// return the initial set
		return initialSet.clone();
	}

	@Override
	protected void flowThrough(Object in, Object node, Object out) {
		// perform flow from in to out, through node
		FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
		Unit unit = (Unit) node;

		// copy inSet to outSet
		inSet.copy(outSet);

		// a. if the stmt is assign stmt
		//      outSet = (inSet - killSet) union genSet;
		//      kill left and left.base if in inSet
		//      gen left if right in inSet
		// b. if contains invoke expr, kill side effect
		// c. otherwise, outSet = inSet (already done above)
		if (unit instanceof AssignStmt) {

			AssignStmt as = (AssignStmt) unit;
			Value left = as.getLeftOp(), right = as.getRightOp();
			Value leftBase = null;
			if (left instanceof InstanceFieldRef) {
				leftBase = ((InstanceFieldRef) left).getBase();
			}

			// kill left.base
			// and check if gen left or if kill left
			int genIndex = -1;
			int killIndex = -1;
			for (Iterator inIt = inSet.iterator(); inIt.hasNext();) {
				IDValuePair rpp = (IDValuePair) inIt.next();
				if (rpp.value.equivTo(right))
					genIndex = rpp.groupID;
				if (rpp.value.equivTo(left)) 
					killIndex = rpp.groupID;
				// if def is InstantFieldRef, soot will not treat the base object as def, 
				// however, here we need to treat base as changed, so kill the base  
				if (leftBase != null && rpp.value.equivTo(leftBase)) {
					outSet.remove(rpp);
					killAlias(outSet, rpp);
				}
			}
			// if both gen left and kill left, do nothing
			// otherwise, kill left or gen left
			if (killIndex != genIndex) {
				if (killIndex != -1) {
					IDValuePair killedPair = new IDValuePair(killIndex, left); 
					outSet.remove(killedPair);
					killAlias(outSet, killedPair);
				}
				if (genIndex != -1)
					outSet.add(new IDValuePair(genIndex, left));
			}
		}
		
		if (unit instanceof Stmt && ((Stmt)unit).containsInvokeExpr()) {
			Stmt stmt = (Stmt)unit;
			//System.out.println("******************" + unit);
			FlowSet tempSet = (FlowSet)outSet.clone();

			for (Iterator tempIt = tempSet.iterator(); tempIt.hasNext(); ) {
				IDValuePair rpp = (IDValuePair) tempIt.next();
				Value v = rpp.value;
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
				
				// write to rpp.value itselft
				if (past.unitCanWriteTo(stmt, v)) {
					//System.out.println("write yy" + v);
					outSet.remove(rpp);
					killAlias(outSet, rpp);
					continue;
				}
				
				if (!immutable) {
					// write to fields of rpp.value
					for (Iterator<FieldRef> frIt = getFieldRefsIn(v).iterator(); frIt.hasNext(); ) {
						FieldRef fr = frIt.next();
						//System.out.println("fieldRef: " + fr);
						if (past.unitCanWriteTo(stmt, fr)) {
							//System.out.println("write yy" + fr);
							//kill current rpp
							outSet.remove(rpp);
							killAlias(outSet, rpp);
							break;
						}
					}
				}
			}
		}

//		System.out.println("--------------------------------\n" + unit + "\ninset: " + inSet + "\noutset: " + outSet);
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
	 * @param pair the pair being killed
	 */
	private void killAlias(FlowSet s, IDValuePair pair) {
		if (! (pair.value instanceof Local)) return;
		
		PointsToSet pairPTSet = pa.reachingObjects((Local)pair.value);
		FlowSet tempSet = (FlowSet)s.clone();
		
		for (Iterator tempIt = tempSet.iterator(); tempIt.hasNext();) {
			IDValuePair rpp = (IDValuePair) tempIt.next();
			if (rpp.groupID == pair.groupID) {
				if (rpp.value instanceof Local) {
					PointsToSet rppPTSet = pa.reachingObjects((Local)rpp.value);
					if (rppPTSet.hasNonEmptyIntersection(pairPTSet)) {
						//kill
						s.remove(rpp);
					}
				}
			}
		}
	}
}
