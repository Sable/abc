package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Map;

import soot.Local;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;

/**
 * A single shadow match for a tracematch symbol.
 * @author Eric Bodden
 */
public class SymbolShadow {
	
	protected String symbolName;
	
	protected TraceMatch owner;
		
	protected Map<String,Local> tmFormalToAdviceLocal;

	protected final String uniqueShadowId;

	SymbolShadow(String symbolName,
			Map<String, Local> tmVarToAdviceLocal, int shadowId, TraceMatch owner) {
		this.symbolName = symbolName;
		this.tmFormalToAdviceLocal = tmVarToAdviceLocal;
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
	
}