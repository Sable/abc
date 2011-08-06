package abc.ja.jpi;

import java.util.Hashtable;

import polyglot.types.SemanticException;
import soot.SootMethod;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.NeverMatch;

public class JPIGlobalAspectInfo extends GlobalAspectInfo {
	
    private Hashtable <SootMethod,MethodAdviceList> adviceLists=null;
	
	@Override
	public void computeAdviceLists() throws SemanticException {
		super.computeAdviceLists();
		
        adviceLists=abc.weaving.matching.AdviceApplication.computeAdviceLists(this);
        
        for(MethodAdviceList mal: adviceLists.values()) {
        	for(AdviceApplication aa: mal.allAdvice()) {
        		@SuppressWarnings("unused")
				Aspect b = aa.advice.getAspect(); //if artificial aspect
        		int l;
        		//aa.setResidue(NeverMatch.v());
        	}
        }
		
	}

}
