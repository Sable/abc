package abc.ja.jpi;

import java.lang.reflect.Field;
import java.util.Hashtable;

import polyglot.types.SemanticException;
import soot.SootMethod;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.NeverMatch;

public class JPIGlobalAspectInfo extends GlobalAspectInfo {
	
    private Hashtable <SootMethod,MethodAdviceList> adviceLists=null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void computeAdviceLists() throws SemanticException {
		super.computeAdviceLists();
		Field fieldAdviceLists = null;
		try {
			fieldAdviceLists = GlobalAspectInfo.class.getDeclaredField("adviceLists");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fieldAdviceLists.setAccessible(true);
		try {
			adviceLists = (Hashtable <SootMethod,MethodAdviceList>)fieldAdviceLists.get(abc.main.Main.v().getAbcExtension().getGlobalAspectInfo());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(SootMethod key : adviceLists.keySet()){
			for(int j=0; j<adviceLists.get(key).bodyAdvice.size();j++){
				AdviceApplication aa = adviceLists.get(key).bodyAdvice.get(j);
				if(aa.advice.getAspect().getName().equals("$dummyAspect$")){
					adviceLists.get(key).bodyAdvice.get(j).setResidue(NeverMatch.v());
				}
			}
			for(int j=0; j<adviceLists.get(key).stmtAdvice.size();j++){
				AdviceApplication aa = adviceLists.get(key).stmtAdvice.get(j);
				if(aa.advice.getAspect().getName().equals("$dummyAspect$")){
					adviceLists.get(key).stmtAdvice.get(j).setResidue(NeverMatch.v());
				}
			}
			for(int j=0; j<adviceLists.get(key).preinitializationAdvice.size();j++){
				AdviceApplication aa = adviceLists.get(key).preinitializationAdvice.get(j);
				if(aa.advice.getAspect().getName().equals("$dummyAspect$")){
					adviceLists.get(key).preinitializationAdvice.get(j).setResidue(NeverMatch.v());
				}
			}
			for(int j=0; j<adviceLists.get(key).initializationAdvice.size();j++){
				AdviceApplication aa = adviceLists.get(key).initializationAdvice.get(j);
				if(aa.advice.getAspect().getName().equals("$dummyAspect$")){
					adviceLists.get(key).initializationAdvice.get(j).setResidue(NeverMatch.v());
				}
			}
		}
	}

}
