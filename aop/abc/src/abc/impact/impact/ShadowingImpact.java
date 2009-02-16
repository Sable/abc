package abc.impact.impact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.SootClass;

public final class ShadowingImpact {
	private final String fieldName;
	private final SootClass originType;
	private final SootClass currentType;
	private final Set<SootClass> affectedTypes;
	
	public ShadowingImpact(final String fieldName, final SootClass originType, final SootClass currentType, final Set<SootClass> affectedTypes) {
		this.fieldName = fieldName;
		this.originType = originType;
		this.currentType = currentType;
		if (affectedTypes == null) this.affectedTypes = new HashSet<SootClass>();
		else this.affectedTypes = new HashSet<SootClass>(affectedTypes);
	}

	/**
	 * Get types affected by this itd field declaration
	 * @return A list of SootClass
	 */
	public Set<SootClass> getAffectedTypes() {
		return Collections.unmodifiableSet(affectedTypes);
	}

	public String getFieldName() {
		return fieldName;
	}
	
	/**
	 * Get type where the field is inherited from originally if no itd field declaration
	 * @return SootClass
	 */
	public SootClass getOriginType() {
		return originType;
	}
	
	/**
	 * Get type where the field is currently declared
	 * @return SootClass
	 */
	public SootClass getCurrentType() {
		return currentType;
	}
	
	@Override
	public String toString() {
		return "field [" + fieldName + "], originally declared in " + originType
		+ ", currently declared in " + currentType + ", affected types " + affectedTypes;
	}
}
