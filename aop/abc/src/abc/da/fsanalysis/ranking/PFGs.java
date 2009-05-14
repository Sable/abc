package abc.da.fsanalysis.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import abc.da.HasDAInfo;
import abc.da.fsanalysis.ranking.Ranking.Features;
import abc.da.fsanalysis.util.ShadowsPerTMSplitter;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.aspectinfo.TracePattern;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;

public class PFGs {

	public void dump(String message, Set<Shadow> inShadows, boolean numberOnly) {
		Set<Shadow> shadows = new HashSet<Shadow>(inShadows);
		
		//remove all non-symbol shadows and all disabled shadows
		for (Iterator<Shadow> iter = shadows.iterator(); iter.hasNext();) {
			Shadow s = iter.next();
			if(!s.isEnabled() || !(s.getAdviceDecl() instanceof PerSymbolTMAdviceDecl)) {
				iter.remove();
			}
		}
		
		//for each TracePattern
		Map<TracePattern, Set<Shadow>> splitSymbolShadows = ShadowsPerTMSplitter.splitSymbolShadows(shadows);		
		for (Map.Entry<TracePattern, Set<Shadow>> entry : splitSymbolShadows.entrySet()) {
			TracePattern tp = entry.getKey();
			Set<Shadow> perTMShadows = entry.getValue();

			//compute symbol names
	        Map<SootMethod,String> adviceMethodToSymbol = new HashMap<SootMethod, String>();
	        for (String sym : tp.getSymbols()) {
				SootMethod adviceMethod = tp.getSymbolAdviceMethod(sym);
				adviceMethodToSymbol.put(adviceMethod,sym);
			}
	        Map<Shadow,String> shadowToSymbolName = new HashMap<Shadow, String>();
	        for (Shadow shadow : perTMShadows) {
				SootMethod adviceMethod = shadow.getAdviceDecl().getImpl().getSootMethod();
				String symbol = adviceMethodToSymbol.get(adviceMethod);
				assert symbol!=null;
				shadowToSymbolName.put(shadow, symbol);
			}
			
	        //remove artificial shadows
			for (Iterator<Shadow> iter = perTMShadows.iterator(); iter.hasNext();) {
				Shadow s = iter.next();
				if(shadowToSymbolName.get(s).equals("newDaCapoRun")) {
					iter.remove();
				}
			}
			
			Set<Shadow> ppfs = filterPotentialPointsOfFailure(perTMShadows);

			Set<Set<Shadow>> groups = new HashSet<Set<Shadow>>();
			for (Shadow ppf : ppfs) {
				Set<Shadow> overlaps = new HashSet<Shadow>(AdviceDependency.getAllEnabledShadowsOverlappingWith(Collections.singleton(ppf)));
				overlaps.add(ppf);
				groups.add(overlaps);
			}
			
			List<Ranking.PotentialFailureGroup> pfgs = new ArrayList<Ranking.PotentialFailureGroup>();
			for (Set<Shadow> group : groups) {
				Set<Shadow> groupPPFs = filterPotentialPointsOfFailure(group);
				Set<Shadow> contextShadows = new HashSet<Shadow>(group);
				contextShadows.removeAll(groupPPFs);
				EnumSet<Features> groupFeatures = EnumSet.noneOf(Features.class);
				for(Shadow s: group) {
					EnumSet<Features> shadowFeatures = Ranking.PotentialFailureGroup.featuresOf(s);
					groupFeatures.addAll(shadowFeatures);
				}
				double rank = 1-(groupFeatures.size()/(Ranking.pPFFeatures.length+0.0));
				pfgs.add(new Ranking.PotentialFailureGroup(groupPPFs, rank,  groupFeatures, contextShadows, tp));
			}
			Collections.sort(pfgs);
			System.err.println("Number of potential failure groups "+message+": "+pfgs.size());
			if(!numberOnly) {
				System.err.println();
				System.err.println();
				System.err.println();
				System.err.println();
				System.err.println();
				for (Ranking.PotentialFailureGroup potentialFailureGroup : pfgs) {
					System.err.println(potentialFailureGroup);
					System.err.println();
					System.err.println();
				}
				System.err.println();
				System.err.println();
				System.err.println();
				System.err.println();
			}
		}
	}	

	/**
	 * This methods takes a set of shadows as argument and returns a fresh set
	 * containing all shadows in <i>shadows</i> that are potential points of failure,
	 * i.e. are final shadows, i.e. shadows of a final symbol that may drive
	 * the TracePattern into a final state.
	 */
	private Set<Shadow> filterPotentialPointsOfFailure(Set<Shadow> shadows) {
		HasDAInfo gai = (HasDAInfo) Main.v().getAbcExtension();

		Set<Shadow> PPFs = new HashSet<Shadow>();
		
    	Map<SootMethod,String> adviceMethodToSymbol = new HashMap<SootMethod, String>();
    	Map<SootMethod,TracePattern> adviceMethodToTracePattern = new HashMap<SootMethod, TracePattern>();
    	for(TracePattern tm : gai.getDependentAdviceInfo().getTracePatterns()) {	        
	        for (String sym : tm.getSymbols()) {
				SootMethod adviceMethod = tm.getSymbolAdviceMethod(sym);
				adviceMethodToSymbol.put(adviceMethod,sym);
				adviceMethodToTracePattern.put(adviceMethod,tm);
			}
    	}
        Set<Shadow> allShadows = new HashSet<Shadow>(shadows);
        for (Shadow s : allShadows) {
			if(s.isEnabled() && (s.getAdviceDecl() instanceof PerSymbolTMAdviceDecl)) {
				SootMethod adviceMethod = s.getAdviceDecl().getImpl().getSootMethod();
				String symbol = adviceMethodToSymbol.get(adviceMethod);
				assert symbol!=null;
				if(adviceMethodToTracePattern.get(adviceMethod).getFinalSymbols().
						contains(adviceMethodToSymbol.get(adviceMethod))) {
					PPFs.add(s);
				}
			}
		}
                
        return PPFs;
	}
	
	
	
	
	
	//singleton pattern
	
	protected static PFGs instance;
	
	private PFGs() {}
	
	public static PFGs v() {
		if(instance==null) {
			instance = new PFGs();
		}
		return instance;
	}
	
	public static void reset() {
		instance=null;
	}


}
