/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package abc.tmwpopt.fsanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.Position;

import soot.Scene;
import soot.SootMethod;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.aspectinfo.TraceMatch;

/**
 * This class produces a ranked list of potential points of failure from a given set of shadows.
 * A potential point of failure is a shadow that may trigger a final edge, i.e. an edge into a final state.
 * The list we return is a list of {@link PotentialFailureGroup}s. Each such group contains one or more 
 * potential points of failure with an equivalent <i>context</i>. The context consists of all shadows that
 * overlap with these potential points of failure.
 *
 */
public class Ranking {
	
	/**
	 * Those are the current features we expose for ranking and filtering. 
	 */
	enum Features {
		ANALYSIS_ABORTED,		
		CALL,
		CONTINUATION,
		DELEGATE,
		DYNAMIC_LOADING,
		NO_CONTEXT,
		OVERLAPS,
		PPF_ANALYSIS_ABORTED,
		PPF_CALL,
		PPF_CONTINUATION,
		PPF_DELEGATE,
		PPF_DYNAMIC_LOADING,
		PPF_NO_CONTEXT
	};
	
	protected static Features[] pPFFeatures = new Features[] {
		Features.PPF_ANALYSIS_ABORTED,
		Features.PPF_CALL,
		Features.PPF_CONTINUATION,
		Features.PPF_DELEGATE,
		Features.PPF_DYNAMIC_LOADING,
		Features.PPF_NO_CONTEXT,
	}; 
	
	/**
	 * A potential failure group has a set of potential points of failures, a rank, a set of overlapping shadows,
	 * a common rank for all theses shadows and a tracematch that owns the group.
	 */
	public static class PotentialFailureGroup implements Comparable<PotentialFailureGroup>{
		protected final Set<Shadow> ppfs;	
		protected final double rank;
		protected final EnumSet<Features> features;
		protected final Set<Shadow> overlaps;
		protected final TraceMatch tm;
		
		public PotentialFailureGroup(Shadow ppf, double rank, EnumSet<Features> features, Set<Shadow> overlaps, TraceMatch tm) {
			this(new HashSet<Shadow>(), rank, ppfFeatures(features,ppf), overlaps, tm);
			this.ppfs.add(ppf);
		}

		private static EnumSet<Features> ppfFeatures(EnumSet<Features> features, Shadow ppf) {
			EnumSet<Features> copy = EnumSet.noneOf(Features.class);
			copy.addAll(features);
			EnumSet<Features> ppfFeaturs = featuresOf(ppf);
			for (Features f : ppfFeaturs) {
				copy.add(Features.values()[f.ordinal()+(Features.PPF_ANALYSIS_ABORTED.ordinal()-Features.ANALYSIS_ABORTED.ordinal())]);
			}
			return copy;
		}

		protected PotentialFailureGroup(Set<Shadow> ppfStrings, double rank, EnumSet<Features> features, Set<Shadow> overlaps, TraceMatch tm) {
			this.rank = rank;
			this.tm = tm;
			this.features = features;
			this.ppfs = ppfStrings;
			this.overlaps = overlaps;
		}
		
		public PotentialFailureGroup tryMerge(PotentialFailureGroup other) {
			if(this==other) {
				return null;
			}
			EnumSet<Features> copy1 = EnumSet.noneOf(Features.class);
			copy1.addAll(features);
			copy1.removeAll(Arrays.asList(pPFFeatures));
			EnumSet<Features> copy2 = EnumSet.noneOf(Features.class);
			copy2.addAll(other.features);
			copy2.removeAll(Arrays.asList(pPFFeatures));
			if(this.tm.equals(other.tm)			 
			 && this.rank== other.rank
			 && copy1.equals(copy2)
			 && this.getAllShadowStrings().equals(other.getAllShadowStrings()) ) {
				Set<Shadow> joinedPPFs = new HashSet<Shadow>(ppfs);
				joinedPPFs.addAll(other.ppfs);

				/* Naive O(n^2) solution to merging: */
				Set<Shadow> joinedOverlaps = new HashSet<Shadow>(overlaps);

				for (Shadow o : other.overlaps) {
					boolean hit = false;
					for (Shadow a : overlaps) {
						if (shadowToString(o).equals(shadowToString(a)))
							{ hit = true; break; }
					}
					if (!hit) joinedOverlaps.add(o);
				}

				for (Shadow p : joinedPPFs) {
					Iterator<Shadow> it = joinedOverlaps.iterator();
					Shadow a;
					while (it.hasNext()) {
						a = it.next();
						if (shadowToString(a).equals(shadowToString(p)))
							it.remove();
					}
				}

				EnumSet<Features> mergedFeatures = EnumSet.noneOf(Features.class);
				mergedFeatures.addAll(this.features);
				mergedFeatures.addAll(other.features);

				return new PotentialFailureGroup(joinedPPFs, rank, mergedFeatures, joinedOverlaps, tm);
			} else {
				return null;
			}			
		}

		public int compareTo(PotentialFailureGroup o) {
			return (o.rank - rank)<0?-1:1;
		}
		
		@Override
		public String toString() {
                        String tmName = tm.getContainerClass().getShortName();
                        tmName += "." + tm.getName();
                        String bmName = Scene.v().getMainClass().getName();
			String qualName = bmName + "." + tmName;
			StringBuffer sb = new StringBuffer();
			sb.append(qualName);
			sb.append(": potential match, ");
			sb.append(Math.round(rank*100));
			sb.append("%, features ");
			sb.append(features.toString());
			sb.append(" - vector[");
			EnumSet<Features> allOf = EnumSet.allOf(Features.class);
			for (Iterator<Features> featIter = allOf.iterator(); featIter.hasNext();) {
				Features feature =  featIter.next();
				if(features.contains(feature)) {
					sb.append("1");
				} else {
					sb.append("0");
				}
				if(featIter.hasNext()) {
					sb.append(";");
				}
			}
			sb.append("]:\nPotential points of failure:\n");
			List<String> ppfStringList = new ArrayList<String>();
			for (Shadow s : ppfs)
				ppfStringList.add(shadowToString(s));

			Collections.sort(ppfStringList);
			for (String s : ppfStringList) {
				sb.append(s);
				sb.append("\n");
			}
			if(!overlaps.isEmpty()) {
				List<String> overlapsList = new ArrayList<String>();
				for (Shadow s : overlaps) {
					overlapsList.add(shadowToString(s));
				}
				Collections.sort(overlapsList);
				sb.append("Related program points:\n");
				for (String s : overlapsList) {
					sb.append(s);
					sb.append("\n");
				}
			}
			return sb.toString();
		}
		
		private static String shadowToString(Shadow s) {
			EnumSet<Features> features = featuresOf(s);			
			String res = "";
			res += SymbolNames.v().symbolNameForShadow(s) + " - ";
			res += s.getContainer();
			if(s.getPosition()!=null) {
				res += " @ line "+s.getPosition().line();
			}
			res += " " + features;
			return res.intern();
		}

		public String ppfAndGroupToHTMLString(String URLPrefix, int pfgNumber) {
			// Partition this failure group by symbol and emit the necessary
			// SuperNote HTML for the failure group.
			StringBuffer res = new StringBuffer("<h1>Potential point of failure "+pfgNumber+"</h1>\n");
			res.append("<script type='text/javascript'>\n");
			res.append("var supernote"+pfgNumber+" = new SuperNote('supernote"+pfgNumber+"', []);\n");
			res.append("</script>\n");

			Set<String> symbols = new HashSet<String>();
			HashMap<String, Set<Shadow>> symToShadows = new HashMap<String, Set<Shadow>>();

			Set<Shadow> ppfPlusOverlaps = new HashSet<Shadow>(ppfs);
			ppfPlusOverlaps.addAll(overlaps);
			
			for (Shadow s : ppfPlusOverlaps) {
				String sym = SymbolNames.v().symbolNameForShadow(s);
				symbols.add (sym);
				Set<Shadow> ss = symToShadows.get(sym);
				if (ss == null) {
					ss = new HashSet<Shadow>();
					symToShadows.put (sym, ss);
				}
				ss.add (s);
			}

			Set<String> unseenSymbols = new HashSet<String>(tm.getSymbols());

			for (String sym : symbols) {
			    unseenSymbols.remove(sym);
				res.append("<div id='supernote"+pfgNumber+"-note-"+sym+"' class='notedefault snp-mouseoffset snb-pinned' style='width:400px'>\n");
				res.append("<h5><a href='#' class='note-close'>X</a>"+sym+"</h5>\n");
				for (Shadow s : symToShadows.get(sym))
					res.append(shadowToHTMLString(URLPrefix, pfgNumber, s));
				res.append("</div>\n\n");
			}
			for (String sym : unseenSymbols) {
				res.append("<div id='supernote"+pfgNumber+"-note-"+sym+"' class='notedefault snp-mouseoffset snb-pinned' style='width:400px'>\n");
				res.append("<h5><a href='#' class='note-close'>X</a>"+sym+"</h5>\n");
				res.append("<p>no matches</p>\n");
				res.append("</div>\n\n");
			}

			return res.toString();
		}

		private static String shadowToHTMLString(String URLPrefix, int pfgNumber, Shadow s) {
			String name = s.getContainer().getDeclaringClass().getName();
			
			Position pos = s.getPosition();			
			int ln = pos==null ? 0 : pos.line();
			
			return "<a name='"+SymbolNames.v().symbolNameForShadow(s)+
				"-"+pfgNumber+"'"+
			    " href='"+URLPrefix+name.replace('.', '/')+".java;line="+ln+"#l_"+ln+"'>"+name+":"+ln+"</a>\n";
		}

		private static EnumSet<Features> featuresOf(Shadow s) {
			EnumSet<Features> features = EnumSet.noneOf(Features.class);
			if(s.pointsToSetsSufferFromDynamicLoading())
				features.add(Features.DYNAMIC_LOADING);
			if(s.isDelegateCallShadow())
				features.add(Features.DELEGATE);
			if(s.notAllPointsToSetsContextSensitive()) {
				features.add(Features.NO_CONTEXT);
			}
			if(shadowsRetainedBecauseOfTainting.contains(s)) {
				features.add(Features.CALL);
			}
			if(shadowsRetainedInterprocedurally.contains(s)) {
				features.add(Features.CONTINUATION);
			}
			if(methodsWithCutOffAnalysis.contains(s.getContainer())) {
				features.add(Features.ANALYSIS_ABORTED);
			}
			return features;
		}
		
		protected Set<String> getAllShadowStrings() {
			Set<String> allShadowStrings = new HashSet<String>();
			for (Shadow s : ppfs)
				allShadowStrings.add(shadowToString(s));

			for (Shadow s : overlaps)
				allShadowStrings.add(shadowToString(s));
			return allShadowStrings;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((features == null) ? 0 : features.hashCode());
			result = prime * result
					+ ((overlaps == null) ? 0 : overlaps.hashCode());
			result = prime * result
					+ ((ppfs == null) ? 0 : ppfs.hashCode());
			long temp;
			temp = Double.doubleToLongBits(rank);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PotentialFailureGroup other = (PotentialFailureGroup) obj;
			if (features == null) {
				if (other.features != null)
					return false;
			} else if (!features.equals(other.features))
				return false;
			if (overlaps == null) {
				if (other.overlaps != null)
					return false;
			} else if (!overlaps.equals(other.overlaps))
				return false;
			if (ppfs == null) {
				if (other.ppfs != null)
					return false;
			} else if (!ppfs.equals(other.ppfs))
				return false;
			if (Double.doubleToLongBits(rank) != Double
					.doubleToLongBits(other.rank))
				return false;
			return true;
		}
		
		
	}
	
	protected static Set<Shadow> shadowsRetainedInterprocedurally = new HashSet<Shadow>();
	
	protected static Set<Shadow> shadowsRetainedBecauseOfTainting = new HashSet<Shadow>();
	
	protected static Set<SootMethod> methodsWithCutOffAnalysis = new HashSet<SootMethod>();
	
	public void addShadowsRetainedInterprocedurally(Collection<Shadow> shadows) {
		shadowsRetainedInterprocedurally.addAll(shadows);
	}
	
	public void addShadowsRetainedBecauseOfTainting(Set<Shadow> shadows) {
		shadowsRetainedBecauseOfTainting.addAll(shadows);
	}
	
	public void addMethodWithCutOffComputation(SootMethod m) {
		methodsWithCutOffAnalysis.add(m);
	}
	
	public List<PotentialFailureGroup> rankAndSort(Set<Shadow> ppfs, TraceMatch tm) {
		Map<Shadow,Set<Shadow>> ppfToOverlaps = new HashMap<Shadow, Set<Shadow>>();
		Set<Shadow> allShadows = new HashSet<Shadow>(ppfs);
		for (Shadow ppf : ppfs) {
			Set<Shadow> overlaps =
				AdviceDependency.getAllEnabledShadowsOverlappingWith(Collections.singleton(ppf));
			for (Iterator<Shadow> iter = overlaps.iterator(); iter.hasNext();) {
				Shadow s = iter.next();
				if(!(s.getAdviceDecl() instanceof PerSymbolTMAdviceDecl) || SymbolNames.v().isArtificialShadow(s)) {
					iter.remove();
				}
			}
			ppfToOverlaps.put(ppf, overlaps);
			allShadows.addAll(overlaps);
		}
		
		Set<PotentialFailureGroup> result = new HashSet<PotentialFailureGroup>();		
		for (Shadow shadow : ppfs) {
			result.add(computeRank(shadow,tm,ppfToOverlaps,allShadows));
		}

		List<PotentialFailureGroup> resultList = new ArrayList<PotentialFailureGroup>(result);				
		Set<PotentialFailureGroup> worklist = new HashSet<PotentialFailureGroup>(result);
		while(!worklist.isEmpty()) {
			//pop first pfg
			Iterator<PotentialFailureGroup> iterator = worklist.iterator();
			PotentialFailureGroup pfg1 = iterator.next();
			iterator.remove();
			//
			for (PotentialFailureGroup pfg2 : result) {
				PotentialFailureGroup merged = pfg1.tryMerge(pfg2);
				if(merged!=null) {
					result.remove(pfg1);
					result.remove(pfg2);
					result.add(merged);
					worklist.add(merged);
					//better break, to avoid a ConcurrentModificationException
					break;
				}				
			}				
		}
			
		resultList = new ArrayList<PotentialFailureGroup>(result);				
		Collections.sort(resultList);
		return resultList;
	}

	private PotentialFailureGroup computeRank(Shadow ppf, TraceMatch tm, Map<Shadow, Set<Shadow>> ppfToOverlaps, Set<Shadow> allShadows) {
		double overlap = overlap(ppf,ppfToOverlaps,allShadows);
		double fractionDelegatingShadows = fractionDelegatingShadows(ppf,ppfToOverlaps);
		double fractionShadowPtsDynamicLoading = fractionShadowPtsDynamicLoading(ppf,ppfToOverlaps);
		double fractionShadowPtsNoContext = fractionShadowPtsNoContext(ppf,ppfToOverlaps);
		double relativeSize = relativeSize(ppf,ppfToOverlaps);
		//following only works after flow-sensitive analysis
		double fractionShadowsRetainedInterprocedurally = fractionShadowsRetainedInterprocedurally(ppf,ppfToOverlaps);
		double fractionShadowsWithCutOffAnalysis = fractionShadowsWithCutOffAnalysis(ppf,ppfToOverlaps);
		double fractionShadowsWithTainting = fractionShadowsWithTainting(ppf,ppfToOverlaps);

		double[] penalties = {
				overlap,
				fractionShadowsRetainedInterprocedurally,
				relativeSize,
				fractionShadowPtsDynamicLoading,
				fractionShadowPtsNoContext,
				fractionShadowsWithCutOffAnalysis,
				fractionShadowsWithTainting,
				fractionDelegatingShadows
		};
		double[] penaltyFactors = {
				2,
				2,
				2,
				5,
				1,
				5,
				1,
				5
		};
		
		double sumFactors = 0, sumFeatures = 0, tempRank = 0;
		for (int i = 0; i < penaltyFactors.length; i++) {
			sumFactors += penaltyFactors[i];
			sumFeatures++;
			tempRank += penaltyFactors[i] * penalties[i];
		}
		
		double rank = 1- (tempRank / sumFactors); 
		
		EnumSet<Features> features = EnumSet.noneOf(Features.class);
		if(overlap>0) features.add(Features.OVERLAPS);
		if(fractionShadowsRetainedInterprocedurally>0) features.add(Features.CONTINUATION);
		if(fractionDelegatingShadows>0) features.add(Features.DELEGATE);
		if(fractionShadowPtsDynamicLoading>0) features.add(Features.DYNAMIC_LOADING);
		if(fractionShadowPtsNoContext>0) features.add(Features.NO_CONTEXT);
		if(fractionShadowsWithCutOffAnalysis>0) features.add(Features.ANALYSIS_ABORTED);
		if(fractionShadowsWithTainting>0) features.add(Features.CALL);
			
		return new PotentialFailureGroup(ppf,rank,features,ppfToOverlaps.get(ppf),tm);
	}

	private double relativeSize(Shadow ppf,Map<Shadow, Set<Shadow>> ppfToOverlaps) {
		Set<Shadow> overlaps = new HashSet<Shadow>(ppfToOverlaps.get(ppf));
		
		int maxSize = 0;
		for (Set<Shadow> shadows : ppfToOverlaps.values()) {
			maxSize = Math.max(maxSize, shadows.size());
		}
				
		//subtract 1 because there is always at least one shadow in a group
		return (overlaps.size() - 1) / (maxSize - 1.0);
	}

	private double fractionShadowPtsDynamicLoading(Shadow ppf,Map<Shadow, Set<Shadow>> ppfToOverlaps) {
		Set<Shadow> overlaps = new HashSet<Shadow>(ppfToOverlaps.get(ppf));
		
		int numDynamicLoading = 0;
		for (Shadow shadow : overlaps) {
			if(shadow.pointsToSetsSufferFromDynamicLoading()) {
				numDynamicLoading++;
			}
		}
		return numDynamicLoading / (overlaps.size() + 0.0);
	}

	private double fractionShadowPtsNoContext(Shadow ppf, Map<Shadow, Set<Shadow>> ppfToOverlaps) {
		Set<Shadow> overlaps = new HashSet<Shadow>(ppfToOverlaps.get(ppf));
		
		int numNoContext = 0;
		for (Shadow shadow : overlaps) {
			if(shadow.notAllPointsToSetsContextSensitive()) {
				numNoContext++;
			}
		}
		return numNoContext / (overlaps.size() + 0.0);
	}

	private double fractionDelegatingShadows(Shadow ppf,Map<Shadow, Set<Shadow>> ppfToOverlaps) {
		Set<Shadow> overlaps = new HashSet<Shadow>(ppfToOverlaps.get(ppf));
		
		int numDel = 0;
		for (Shadow shadow : overlaps) {
			if(shadow.isDelegateCallShadow()) {
				numDel++;
			}
		}
		return numDel / (overlaps.size() + 0.0);
	}

	private double fractionShadowsWithTainting(Shadow ppf, Map<Shadow, Set<Shadow>> ppfToOverlaps) {
		Set<Shadow> overlaps = new HashSet<Shadow>(ppfToOverlaps.get(ppf));

		Set<Shadow> taintedShadows = new HashSet<Shadow>(overlaps);
		taintedShadows.retainAll(shadowsRetainedBecauseOfTainting);
		
		return taintedShadows.size() / (overlaps.size() + 0.0);
	}

	private double fractionShadowsWithCutOffAnalysis(Shadow ppf, Map<Shadow, Set<Shadow>> ppfToOverlaps) {
		Set<Shadow> overlaps = new HashSet<Shadow>(ppfToOverlaps.get(ppf));
		
		int cutOff = 0;
		for (Shadow shadow : overlaps) {
			if(methodsWithCutOffAnalysis.contains(shadow.getContainer())) {		
				overlaps.add(ppf);
				cutOff++;
			}
		}
		
		return cutOff / (overlaps.size() + 0.0);
	}

	private double fractionShadowsRetainedInterprocedurally(Shadow ppf,Map<Shadow, Set<Shadow>> ppfToOverlaps) {
		Set<Shadow> overlaps = new HashSet<Shadow>(ppfToOverlaps.get(ppf));

		Set<Shadow> interprocedurallyRetainedShadows = new HashSet<Shadow>(overlaps);
		interprocedurallyRetainedShadows.retainAll(shadowsRetainedInterprocedurally);
		
		return interprocedurallyRetainedShadows.size() / (overlaps.size() + 0.0);
	}

	private double overlap(Shadow ppf, Map<Shadow, Set<Shadow>> ppfToOverlaps, Set<Shadow> allShadows) {
		Set<Shadow> overlaps = new HashSet<Shadow>(ppfToOverlaps.get(ppf));

		Set<Shadow> nonUniqueShadows = new HashSet<Shadow>();
		for (Map.Entry<Shadow, Set<Shadow>> entry : ppfToOverlaps.entrySet()) {
			if(!entry.getKey().equals(ppf)) {
				for (Shadow shadow : entry.getValue()) {
					if(overlaps.contains(shadow)) {
						nonUniqueShadows.add(shadow);
					}
				}
			}			
		}
		
		return nonUniqueShadows.size() / (allShadows.size() + 0.0); 
	}
	
	//singleton pattern
	
	protected static Ranking instance;

    private Ranking() { }

    public static Ranking v() {
		if(instance==null) {
			instance = new Ranking();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
		methodsWithCutOffAnalysis.clear();
		shadowsRetainedBecauseOfTainting.clear();
		shadowsRetainedInterprocedurally.clear();
	}

}
