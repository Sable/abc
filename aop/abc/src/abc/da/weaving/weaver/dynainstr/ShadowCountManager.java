/*
 * Created on 27-Jul-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.da.weaving.weaver.dynainstr;

import abc.da.weaving.weaver.depadviceopt.ds.Shadow;

public class ShadowCountManager {
	
	public static void setCountResidues() {
		for (Shadow shadow : Shadow.reachableActiveShadows()) {
			shadow.conjoinResidueWith(new ShadowCountResidue(DynamicInstrumenter.v().getCodeGenNumber(shadow)));
		}
	}

}
