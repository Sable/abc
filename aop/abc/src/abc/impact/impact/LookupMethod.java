package abc.impact.impact;

import soot.SootClass;

public final class LookupMethod {

	private final SootClass type;
	private final MethodSignature mSig;
	
	public LookupMethod(final SootClass type, final MethodSignature sig) {
		this.type = type;
		mSig = sig;
	}

	public MethodSignature getMSig() {
		return mSig;
	}

	public SootClass getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((mSig == null) ? 0 : mSig.hashCode());
		result = PRIME * result + ((type == null) ? 0 : type.hashCode());
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
		final LookupMethod other = (LookupMethod) obj;
		if (mSig == null) {
			if (other.mSig != null)
				return false;
		} else if (!mSig.equals(other.mSig))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("method invocation [");
		result.append(mSig);
		result.append("] on type ");
		result.append(type);
		
		return result.toString();
	}
}
