/*
 * Created on 27-Jul-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.dynainst;

import soot.SootMethod;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.util.Naming;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.AdviceApplicationVisitor;
import abc.weaving.weaver.AdviceApplicationVisitor.AdviceApplicationHandler;

public class ShadowCountManager {
	
	public static void setCountResidues() {
        AdviceApplicationVisitor.v().traverse(
            new AdviceApplicationHandler() {
            
                public void adviceApplication(AdviceApplication aa,SootMethod context) {
                    //if we have a tracematch symbol advice
                    if(aa.advice instanceof PerSymbolTMAdviceDecl) {
                        PerSymbolTMAdviceDecl decl = (PerSymbolTMAdviceDecl) aa.advice;
                        String traceMatchID = decl.getTraceMatchID();
                        String uniqueShadowId = Naming.uniqueShadowID(traceMatchID, decl.getSymbolId(),aa.shadowmatch.shadowId).intern();
                        Residue originalResidue = aa.getResidue();
                        //conjoin residue, shadow count residue last!
                        int shadowNumber = ShadowRegistry.v().numberOf(uniqueShadowId);
						aa.setResidue(
                                AndResidue.construct(                   
                                        originalResidue,
                                        new ShadowCountResidue(shadowNumber)
                                )
                        );          
                    }
                }
            }
        );
	}

}
