package abc.impact;

import java.util.List;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.ReweavingAnalysis;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.ReweavingPass.ID;

public class AbcExtension extends abc.main.AbcExtension {

	private static final ID PASS_IMPACT_ANALYSIS = new ID("impact");
	
	protected void createReweavingPasses(List passes) {
		super.createReweavingPasses(passes);
		
		ReweavingAnalysis ana = new ImpactAnalysisImpl();
		passes.add( new ReweavingPass(PASS_IMPACT_ANALYSIS, ana) );
	}

	protected GlobalAspectInfo createGlobalAspectInfo() {

		return new ImpactGlobalAspectInfo();
	}
}
