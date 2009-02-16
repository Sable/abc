package abc.impact;

import java.util.Map;
import java.util.Set;

import abc.impact.impact.AdviceImpact;
import abc.impact.impact.AspectImpact;
import abc.impact.impact.ComputationImpact;
import abc.impact.impact.InAspectImpact;
import abc.impact.impact.LookupImpact;
import abc.impact.impact.ShadowingImpact;
import abc.impact.impact.StateImpact;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.InAspect;

public final class ImpactDatabase {
	
	// prevent instantiation
	private ImpactDatabase() {}
	
	/**
	 * get state impact grouped by advice
	 * @return
	 */
	public static Map<AdviceDecl, Set<StateImpact>> getStateImpact() {
		return ImpactAnalysisImpl.getStateImpactMap();
	}

	/**
	 * Get computation impact grouped by advice
	 * @return
	 */
	public static Map<AdviceDecl, ComputationImpact> getComputationImpact() {
		return ImpactAnalysisImpl.getComputationImpactMap();
	}

	/**
	 * get shadowing impact grouped by static crosscutting statement
	 * @return
	 */
	public static Map<InAspect, Set<ShadowingImpact>> getShadowingImpact() {
		return ImpactAnalysisImpl.getShadowingImpactMap();
	}

	/**
	 * get lookup impact grouped by static crosscutting statement
	 * @return
	 */
	public static Map<InAspect, Set<LookupImpact>> getLookupImpact() {
		return ImpactAnalysisImpl.getLookupImpactMap();
	}

	/**
	 * get state and computation impact grouped by advice
	 * @return
	 */
	public static Map<AdviceDecl, AdviceImpact> getAdviceImpactMap() {
		return ImpactAnalysisImpl.getAdviceImpactMap();
	}

	/**
	 * get shadowing and lookup impact grouped by static crosscutting statement
	 * @return
	 */
	public static Map<InAspect, InAspectImpact> getInAspectImpactMap() {
		return ImpactAnalysisImpl.getInAspectImpactMap();
	}

	/**
	 * get impact grouped by aspect, advice/static-crosscutting
	 * @return
	 */
	public static Map<Aspect, AspectImpact> getAspectImpactMap() {
		return ImpactAnalysisImpl.getAspectImpactMap();
	}

	/**
	 * get impact grouped by package, apsect, advice/static-crosscutting
	 * @return
	 */
	public static Map<String, Map<Aspect, AspectImpact>> getPackageImpactMap() {
		return ImpactAnalysisImpl.getPackageImpactMap();
	}
	
	
}
