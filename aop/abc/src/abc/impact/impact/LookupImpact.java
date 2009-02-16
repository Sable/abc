package abc.impact.impact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class LookupImpact {
	
	private final LookupMethod lookupMethod;
	private final Set<LookupChange> lookupChanges;
	
	
	public LookupImpact(final LookupMethod lookupMethod, final Set<LookupChange> lookupChanges) {
		this.lookupMethod = lookupMethod;
		if (lookupChanges == null) this.lookupChanges = new HashSet<LookupChange>();
		else this.lookupChanges = new HashSet<LookupChange>(lookupChanges);
	}
	
	public Set<LookupChange> getLookupChanges() {
		return Collections.unmodifiableSet(lookupChanges);
	}

	public LookupMethod getLookupMethod() {
		return lookupMethod;
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((lookupChanges == null) ? 0 : lookupChanges.hashCode());
		result = PRIME * result + ((lookupMethod == null) ? 0 : lookupMethod.hashCode());
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
		final LookupImpact other = (LookupImpact) obj;
		if (lookupChanges == null) {
			if (other.lookupChanges != null)
				return false;
		} else if (!lookupChanges.equals(other.lookupChanges))
			return false;
		if (lookupMethod == null) {
			if (other.lookupMethod != null)
				return false;
		} else if (!lookupMethod.equals(other.lookupMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(lookupMethod);
		result.append(" ");
		result.append(lookupChanges);
		
		return result.toString();
	}
}
