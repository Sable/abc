package abc.impact.impact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.jimple.Stmt;

final public class ComputationImpact {
	
	public static enum ComputationImpactType {
		ADDITION ("addition"),
		ELIMINATION ("elimination"),
		DEFINITE_SUBSTITUTION ("definite-substitution"),
		CONDITIONAL_SUBSTITUTION ("conditional-substitution"),
		MIXED ("mixed"),
		INVARIANT ("invariant");
		
		private String desc;

		private ComputationImpactType(String desc) {
			this.desc = desc;
		}

		@Override
		public String toString() {
			return desc;
		}
	}
	
//	public static final int ADDITION = 1;
//	public static final int ELIMINATION = 2;
//	public static final int MUST_SUBSTITUTION = 3;
//	public static final int MAY_SUBSTITUTION = 4;
//	public static final int INVARIANT = 5;
//	public final String [] typeNames = {"addition", "elimination", "must-substitution", "may-substitution", "invariant"};

	private final ComputationImpactType type;
	private final Set<Stmt> evidences; /* currently all exactproceed stmt */
	
	public ComputationImpact(ComputationImpactType type) {
		this(type, null);
	}
	
	/**
	 * 
	 * @param type
	 * @param evidences should be a Set of exact-proceed calls
	 */
	public ComputationImpact(ComputationImpactType type, Set<Stmt> evidences) {
		this.type = type;
		if (evidences == null) this.evidences = new HashSet<Stmt>();
		else this.evidences = new HashSet<Stmt>(evidences);
	}
	
	/**
	 * Evidence is not implemented
	 * @return
	 */
	public Set<Stmt> getEvidences() {
		return Collections.unmodifiableSet(evidences);
	}

	public ComputationImpactType getType() {
		return type;
	}
	
//	public String getTypeName() {
//
//		if (type == ADDITION 
//				|| type == ELIMINATION
//				|| type == MUST_SUBSTITUTION
//				|| type == MAY_SUBSTITUTION
//				|| type == INVARIANT)
//		{
//			return typeNames[type-1];
//		} else 
//			return "Unkown type";
//	}

	public String toString() {
		return type + " computation impact";
		// TODO since exact proceed contains no line# information,
		// cannot output evidence; if abc is improved so that
		// proceed contains line#, output evidences;
		// get position of proceed calls
//		if (impact.evidences != null && ! impact.evidences.isEmpty()) {
//		System.out.println("evidence:");

//		AbcClass ac = adviceDecl.getImpl().getDeclaringClass();
//		SootClass sc = ac.getSootClass();
//		String fileName = ((SourceFileTag) sc.getTag("SourceFileTag")).getAbsolutePath();

//		for (Iterator smIt = impact.evidences.iterator(); smIt.hasNext(); ) {
//		Stmt adviceStmt = (Stmt)smIt.next();

//		SourceLnPosTag slpTag = (SourceLnPosTag) adviceStmt.getTag("SourceLnPosTag");
//		Position pos;
//		if (slpTag != null) {
//		pos = new ComparablePosition(fileName, slpTag.startLn(),
//		slpTag.startPos(), slpTag.endLn(), slpTag.endPos());
//		System.out.println("\t" + pos);
//		}
//		}
//		}
		//return type + " computation impact: " + evidences;
	}
}
