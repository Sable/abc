/**
 * 
 */
package abc.impact.impact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import polyglot.util.Position;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;
import soot.tagkit.SourceFileTag;
import soot.tagkit.SourceLnPosTag;
import abc.impact.utils.ComparablePosition;

public final class Mutation {
	private final SootMethod sootMethod;
	private final Stmt mutateStmt;
	private final String mutatedFieldName;
	private final SootClass mutatedFieldDeclClass;
	private final Set<Type/*should all RefType*/> mutatedTypes;

	public Mutation(final SootMethod sootMethod, final Stmt mutateStmt, final String mutatedFieldName, final SootClass declClass, final Set<Type> mutatedTypes) {
		this.sootMethod = sootMethod;
		this.mutateStmt = mutateStmt;
		this.mutatedFieldName = mutatedFieldName;
		this.mutatedFieldDeclClass = declClass;
		if (mutatedTypes == null) this.mutatedTypes = new HashSet<Type>();
		else this.mutatedTypes = new HashSet<Type>(mutatedTypes);
	}

	public String getMutatedFieldName() {
		return mutatedFieldName;
	}

	/**
	 * May return null.
	 */
	public SootClass getMutatedFieldDeclClass() {
		return mutatedFieldDeclClass;
	}

	public Set<Type> getMutatedTypes() {
		return Collections.unmodifiableSet(mutatedTypes);
	}

	public Stmt getMutateStmt() {
		return mutateStmt;
	}

	public SootMethod getSootMethod() {
		return sootMethod;
	}
	
	public Position getPosition() {
		String fileName = ((SourceFileTag) sootMethod.getDeclaringClass().getTag("SourceFileTag")).getAbsolutePath();
		SourceLnPosTag slpTag = (SourceLnPosTag) mutateStmt.getTag("SourceLnPosTag");

		Position pos;
		if (slpTag != null) {
			pos = new ComparablePosition(fileName, slpTag.startLn(),
					slpTag.startPos(), slpTag.endLn(), slpTag.endPos());
		} else {
			pos = new Position(fileName);
		}
		return pos;
	}

	public String toString() {
		
		return getPosition().toString()
				+ " field ["
				+ mutatedFieldName
				+ "]"
				+ (mutatedFieldDeclClass != null ? ("(declared in "
						+ mutatedFieldDeclClass + ")") : "") + " in "
				+ mutatedTypes;
	}
}