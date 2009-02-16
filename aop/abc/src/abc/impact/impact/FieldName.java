package abc.impact.impact;

import java.util.Map;

import soot.SootField;
import abc.impact.analysis.ITDNoiseAnalysis;
import abc.weaving.aspectinfo.InAspect;
import abc.weaving.aspectinfo.IntertypeFieldDecl;

final public class FieldName {

	private final String fieldName;

	public FieldName(final String fieldName) {
		this.fieldName = fieldName;
	}
	
	/**
	 * Create the FieldName of a SootField, but take the affect of
	 * itd field injection into account (fix field name)
	 * @param sf
	 */
	public FieldName(final SootField sf) {
		Map<SootField, InAspect> targets = ITDNoiseAnalysis.v().getTargetFields();
		Map<SootField, InAspect> noise = ITDNoiseAnalysis.v().getNoiseFields();
		if (targets.containsKey(sf)) {
			IntertypeFieldDecl fd = (IntertypeFieldDecl) targets.get(sf);
			this.fieldName = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(fd.getTarget());
		} else if (noise.containsKey(sf)) {
			IntertypeFieldDecl fd = (IntertypeFieldDecl) noise.get(sf);
			this.fieldName = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(fd.getTarget());
		} else {
			this.fieldName = sf.getName();
		}
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FieldName other = (FieldName) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return fieldName;
	}
}
