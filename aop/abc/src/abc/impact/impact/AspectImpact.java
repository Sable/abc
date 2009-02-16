package abc.impact.impact;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.InAspect;

/**
 * Impact caused by an aspect
 * @author Dehua Zhang
 *
 */
public final class AspectImpact {
	final private Map<AdviceDecl, AdviceImpact> adviceImpactMap;
	final private Map<InAspect, InAspectImpact> inAspectImpactMap;
	
	public AspectImpact(final Map<AdviceDecl, AdviceImpact> adviceImpactMap, final Map<InAspect, InAspectImpact> inAspectImpactMap) {
		if (adviceImpactMap == null) this.adviceImpactMap = new HashMap<AdviceDecl, AdviceImpact>();
		else this.adviceImpactMap = new HashMap<AdviceDecl, AdviceImpact>(adviceImpactMap);
		if (inAspectImpactMap == null) this.inAspectImpactMap = new HashMap<InAspect, InAspectImpact>();
		else this.inAspectImpactMap = new HashMap<InAspect, InAspectImpact>(inAspectImpactMap);
	}

	public Map<AdviceDecl, AdviceImpact> getAdviceImpactMap() {
		return Collections.unmodifiableMap(adviceImpactMap);
	}

	public Map<InAspect, InAspectImpact> getInAspectImpactMap() {
		return Collections.unmodifiableMap(inAspectImpactMap);
	}
	
	
}
