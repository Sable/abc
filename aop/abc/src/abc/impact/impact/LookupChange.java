package abc.impact.impact;

import soot.SootMethod;

public final class LookupChange {
	
	private final ReferredPlace invocationPlace;
	private final SootMethod originMethod;
	private final SootMethod currentMethod;
	
	public LookupChange(final ReferredPlace invocationPlace, final SootMethod originMethod, final SootMethod currentMethod) {
		this.invocationPlace = invocationPlace;
		this.originMethod = originMethod;
		this.currentMethod = currentMethod;
	}

	public SootMethod getCurrentMethod() {
		return currentMethod;
	}

	public ReferredPlace getInvocationPlace() {
		return invocationPlace;
	}

	public SootMethod getOriginMethod() {
		return originMethod;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((currentMethod == null) ? 0 : currentMethod.hashCode());
		result = PRIME * result + ((invocationPlace == null) ? 0 : invocationPlace.hashCode());
		result = PRIME * result + ((originMethod == null) ? 0 : originMethod.hashCode());
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
		final LookupChange other = (LookupChange) obj;
		if (currentMethod == null) {
			if (other.currentMethod != null)
				return false;
		} else if (!currentMethod.equals(other.currentMethod))
			return false;
		if (invocationPlace == null) {
			if (other.invocationPlace != null)
				return false;
		} else if (!invocationPlace.equals(other.invocationPlace))
			return false;
		if (originMethod == null) {
			if (other.originMethod != null)
				return false;
		} else if (!originMethod.equals(other.originMethod))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("within ");
		result.append(invocationPlace);
		result.append(", originally matched to ");
		result.append(originMethod);
		result.append(", currently matches to ");
		result.append(currentMethod);
		
		return result.toString();
	}
}
