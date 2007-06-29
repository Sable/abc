package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Map;

import soot.Local;
import soot.SootMethod;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;

/**
 * A single shadow match for a tracematch symbol.
 * @author Eric Bodden
 */
public class SymbolShadow {
	
	protected final String symbolName;
	
    protected final SootMethod container;

    protected final TraceMatch owner;
		
	protected final Map<String,Local> tmFormalToAdviceLocal;

	protected final String uniqueShadowId;

	SymbolShadow(String symbolName,
			Map<String, Local> tmVarToAdviceLocal, int shadowId, SootMethod container, TraceMatch owner) {
		this.symbolName = symbolName;
		this.tmFormalToAdviceLocal = tmVarToAdviceLocal;
        this.container = container;
		this.owner = owner;
		this.uniqueShadowId = Naming.uniqueShadowID(owner.getName(),symbolName,shadowId).intern();
	}

	/**
	 * @return the symbolName
	 */
	public String getSymbolName() {
		return symbolName;
	}

	/**
	 * @return the owner
	 */
	public TraceMatch getOwner() {
		return owner;
	}

	/**
	 * @return the tmFormalToAdviceLocal
	 */
	public Map<String, Local> getTmFormalToAdviceLocal() {
		return tmFormalToAdviceLocal;
	}
	
	/**
	 * @return <code>true</code> if this shadow is enabled in the {@link ShadowRegistry}
	 */
	public boolean isEnabled() {
		return ShadowRegistry.v().isEnabled(getUniqueShadowId());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "symbol:  " + symbolName + "\n" +
			"tracematch: " + owner.getName()+ "\n" +
			"variables:  " + tmFormalToAdviceLocal + "\n" +
			"shadow:     " + uniqueShadowId;				
	}

	public String getUniqueShadowId() {
		return uniqueShadowId;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result
				+ ((symbolName == null) ? 0 : symbolName.hashCode());
		result = prime
				* result
				+ ((tmFormalToAdviceLocal == null) ? 0 : tmFormalToAdviceLocal
						.hashCode());
		result = prime * result
				+ ((uniqueShadowId == null) ? 0 : uniqueShadowId.hashCode());
		return result;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SymbolShadow other = (SymbolShadow) obj;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (symbolName == null) {
			if (other.symbolName != null)
				return false;
		} else if (!symbolName.equals(other.symbolName))
			return false;
		if (tmFormalToAdviceLocal == null) {
			if (other.tmFormalToAdviceLocal != null)
				return false;
		} else if (!tmFormalToAdviceLocal.equals(other.tmFormalToAdviceLocal))
			return false;
		if (uniqueShadowId == null) {
			if (other.uniqueShadowId != null)
				return false;
		} else if (!uniqueShadowId.equals(other.uniqueShadowId))
			return false;
		return true;
	}

    /**
     * @return the container
     */
    public SootMethod getContainer() {
        return container;
    }
	
}