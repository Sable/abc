package abc.impact.utils;

import java.util.Comparator;

import soot.RefType;
import soot.SootMethod;

import abc.impact.impact.MethodSignature;

public class SpecificSootMethodComparator implements Comparator<SootMethod> {

	/**
	 * Order SootMethod according to more specfici rule JLS v2 15.12.2.2
	 * Note: this comparator imposes orderings that are inconsistent with equals 
	 *       tm and um should have the same name and the same size of paramters
	 * @param tm should be SootMethod
	 * @param um should be SootMethod
	 * @return positive if tm is more specific than um
	 *         zero if tm is not more specific than um, and um is not more specific than tm
	 *         negative if um is more specific than um  
	 */
	public int compare(SootMethod tm, SootMethod um) {
		
		SootMethod tsm = (SootMethod)tm;
		SootMethod usm = (SootMethod)um;
		
		RefType t = RefType.v(tsm.getDeclaringClass());
		RefType u = RefType.v(usm.getDeclaringClass());

		MethodSignature tms = new MethodSignature(tsm);
		MethodSignature ums = new MethodSignature(usm);

		
		if (ImpactUtil.isMethodInvocationConversion(t, u) && tms.isMoreSpecificThan(ums)) {
			return 1;
		}
		
		if (ImpactUtil.isMethodInvocationConversion(u, t) && ums.isMoreSpecificThan(tms)) {
			return -1;
		}
		
		return 0;
	}

}
