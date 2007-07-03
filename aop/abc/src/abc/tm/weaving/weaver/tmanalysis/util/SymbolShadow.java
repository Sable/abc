package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.SymbolShadowWithPTS;

/**
 * A symbol shadow represents a static point in the program where the state
 * machine may make a transition with a certain symbol. Each Shadow belongs to a container
 * method and has a uniqueShadowId. Further, it has a mapping from
 * tracematch variables to Soot {@link Local}s (<i>advice actuals</i>)
 * that bind those tracematch variables at the shadow. 
 *
 * @author Eric Bodden
 */
public class SymbolShadow implements ISymbolShadow {
	
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
	 * {@inheritDoc}
	 */
	public String getSymbolName() {
		return symbolName;
	}

	/** 
	 * {@inheritDoc}
	 */
	public TraceMatch getOwner() {
		return owner;
	}

	/** 
	 * {@inheritDoc}
	 */
	public Set<String> getBoundTmFormals() {
		return tmFormalToAdviceLocal.keySet();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public Collection<Local> getAdviceLocals() {
		return tmFormalToAdviceLocal.values();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public Local getAdviceLocalForVariable(String tracematchVariable){
		assert getBoundTmFormals().contains(tracematchVariable);
		return tmFormalToAdviceLocal.get(tracematchVariable);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public Map<String,Local> getTmFormalToAdviceLocal() {
		return new HashMap<String,Local>(tmFormalToAdviceLocal);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		return ShadowRegistry.v().isEnabled(getUniqueShadowId());
	}

	/** 
	 * {@inheritDoc}
	 */
	public String getUniqueShadowId() {
		return uniqueShadowId;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public String getLocationId() {
		return Naming.locationID(uniqueShadowId);
	}
	
    /** 
	 * {@inheritDoc}
	 */
    public SootMethod getContainer() {
        return container;
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
	 * {@inheritDoc}
	 */
	public String toString() {
		return "symbol:  " + symbolName + "\n" +
			"tracematch: " + owner.getName()+ "\n" +
			"variables:  " + tmFormalToAdviceLocal + "\n" +
			"shadow:     " + uniqueShadowId;				
	}

	/**
	 * For a given set of {@link SymbolShadowWithPTS}s, returns the set of their
	 * unique shadow IDs.
	 * @param shadows a set of {@link SymbolShadowWithPTS}s
	 * @return the set of their shadow IDs
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public static Set<String> uniqueShadowIDsOf(Set<ISymbolShadow> shadows) {
		Set ids = new HashSet();
		for (Iterator<ISymbolShadow> shadowIter = shadows.iterator(); shadowIter.hasNext();) {
            ISymbolShadow shadow = shadowIter.next();
			ids.add(shadow.getUniqueShadowId());
		}
		return Collections.unmodifiableSet(ids);
	}
}