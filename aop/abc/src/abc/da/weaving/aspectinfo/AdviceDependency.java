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
package abc.da.weaving.aspectinfo;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.Position;
import soot.SootMethod;
import abc.da.HasDAInfo;
import abc.da.weaving.weaver.depadviceopt.ds.Bag;
import abc.da.weaving.weaver.depadviceopt.ds.HashBag;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.da.weaving.weaver.depadviceopt.ds.ShadowComparator;
import abc.main.Main;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.weaver.AdviceApplicationVisitor;

/**
 * Internal representation of an advice dependency.
 * Such dependencies can either be generated directly from source, via a {@link abc.da.ast.AdviceDependency},
 * or they can be created in the back-end, e.g. for tracematches.
 * @author Eric Bodden
 */
public class AdviceDependency {
	
	/**
	 * A mapping from strong advice names to their variable names.
	 * The advice names are fully qualified. The advice names might contain wildcards,
	 * prefixed with {@link abc.da.ast.AdviceDependency#WILDCARD}.
	 */
	protected final Map<String,List<String>> strongAdviceNameToVars;
	
	/**
	 * A mapping from weak advice names to their variable names.
	 * The advice names are fully qualified. The advice names might contain wildcards,
	 * prefixed with {@link abc.da.ast.AdviceDependency#WILDCARD}.
	 */
	protected final Map<String,List<String>> weakAdviceNameToVars;
	
	/**
	 * Set of all variable names used in strong and weak advice. Exists merely for convenience.
	 */
	protected final Set<String> variables;

	/**
	 * The aspect containing this advice dependency.
	 */
	protected final Aspect container;
	
	/**
	 * A comparator that can compare two shadows from advice in this dependency. 
	 */
	protected final ShadowComparator comparator;

	/**
	 * Position of the dependency declaration in code. 
	 */
	protected final Position pos;
	
	/**
	 * A fixed universe of shadows of dependent advice.
	 */
	protected FixedUniverse<Shadow> fixedUniverse;

	protected ShadowGroupsRecord shadowGroupsRecord;

	/**
	 * Creates a new advice dependency.
	 * @param strongAdviceNameToVars a mapping from names of strong advice (not qualified) to an
	 * 		ordered list of their variables, as mentioned in the dependency declaration
	 * @param weakAdviceNameToVars a mapping from names of weak advice (not qualified) to an
	 * 		ordered list of their variables, as mentioned in the dependency declaration
	 * @param container the aspect containing the dependency declaration
	 * @param pos the position at which the declaration was specified in code
	 */
	public AdviceDependency(Map<String,List<String>> strongAdviceNameToVars, Map<String,List<String>> weakAdviceNameToVars, Aspect container, Position pos) {

		this.pos = pos;
		this.strongAdviceNameToVars = new HashMap<String, List<String>>();
		for (String adviceName : strongAdviceNameToVars.keySet()) {
			if(adviceName.contains(".")) {
				throw new IllegalArgumentException("Advice may not be qualified!");
			}
			//qualify name
			this.strongAdviceNameToVars.put(container.getName()+"."+adviceName, strongAdviceNameToVars.get(adviceName));
		}
		this.weakAdviceNameToVars = new HashMap<String, List<String>>();
		for (String adviceName : weakAdviceNameToVars.keySet()) {
			if(adviceName.contains(".")) {
				throw new IllegalArgumentException("Advice may not be qualified!");
			}
			//qualify name
			this.weakAdviceNameToVars.put(container.getName()+"."+adviceName, weakAdviceNameToVars.get(adviceName));
		}

		
		this.container = container;
		this.variables = new HashSet<String>();
		for (List<String> vars : strongAdviceNameToVars.values()) {
			variables.addAll(vars);
		}
		for (List<String> vars : weakAdviceNameToVars.values()) {
			variables.addAll(vars);
		}
		
		Map<String, List<String>> allAdviceNamesToVars = new HashMap<String, List<String>>();
		allAdviceNamesToVars.putAll(this.strongAdviceNameToVars);
		allAdviceNamesToVars.putAll(this.weakAdviceNameToVars);
		comparator = new ShadowComparator(allAdviceNamesToVars);		
	}

	/**
	 * Returns <code>true</code>, if this group fulfils the quick check, i.e. if
	 * {@link AbstractAdviceDecl#getApplCount()} is larger than 0 for all strong advice. 
	 */
	public boolean fulfillsQuickCheck() {
		final Set<String> strongAdviceNamesThatDidNotMatch = new HashSet<String>(strongAdviceNameToVars.keySet());
		final DAInfo dai = ((HasDAInfo)Main.v().getAbcExtension()).getDependentAdviceInfo();
		AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {

			public void adviceApplication(AdviceApplication aa, SootMethod m) {
				boolean isDependent = dai.isDependentAdvice(aa.advice);
				if(aa.advice.getAspect().equals(container) && isDependent && aa.advice.getApplCount()>0) {
						//found a matched dependent advice; remove it from the set of strong advice
						strongAdviceNamesThatDidNotMatch.remove(
								dai.replaceForHumanReadableName(dai.qualifiedNameOfAdvice((AdviceDecl) aa.advice))
						);					
				}
			}
			
		});
				
		return strongAdviceNamesThatDidNotMatch.isEmpty();
	}

	/**
	 * Returns the aspect containing this dependency.
	 */
	public Aspect getContainer() {
		return container;
	}

	/**
	 * Returns <code>true</code> if this dependency contains an advice with the given name.
	 */
	public boolean containsAdviceNamed(String adviceName) {
		return strongAdviceNameToVars.keySet().contains(adviceName) ||
		       weakAdviceNameToVars.keySet().contains(adviceName);
	}
	
	/**
	 * Returns the set of all advice names.
	 */
	protected Set<String> adviceNames() {
		Set<String> res = new HashSet<String>(strongAdviceNameToVars.keySet());
		res.addAll(weakAdviceNameToVars.keySet());
		return res;
	}
	
	/**
	 * Returns the position of this dependency in code.
	 */
	public Position getPosition() {
		return pos;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("strong: ");
		mapToString(strongAdviceNameToVars, sb);
		
		if(!weakAdviceNameToVars.isEmpty()) {
			sb.append("weak: ");
			mapToString(weakAdviceNameToVars, sb);
		}

		return sb.toString();
	}

	private void mapToString(Map<String, List<String>> map, StringBuffer sb) {
		for (Iterator<Map.Entry<String, List<String>>> entryIter =
				map.entrySet().iterator(); entryIter.hasNext();) {
			Map.Entry<String, List<String>> entry = entryIter.next();
			//print advice name
			sb.append(entry.getKey());
			List<String> args = entry.getValue();
			if(!args.isEmpty()) {
				sb.append("(");
				for (Iterator<String> iterator = args.iterator(); iterator.hasNext();) {
					String varName = iterator.next();
					sb.append(varName);
					if(iterator.hasNext())
						sb.append(",");
				}
				sb.append(")");
			}
			if(entryIter.hasNext()) {
				sb.append(",");
			}
			sb.append(" ");
		}
		sb.append("\n");
	}
	
	public boolean keepsShadowAlive(Shadow s) {
		if(shadowGroupsRecord==null) {
			throw new IllegalStateException("Shadow groups have not yet been computed.");			
		}
		return shadowGroupsRecord.keepsShadowAlive(s);
	}
	
	/**
     * This data structure keeps track of shadow groups in form of a lower-triangular boolean matrix, which
     * tells for shadows with numbers i,j whether the shadows with these numbers have compatible bindings.
     * The indices i,j are internal indices, starting at 0 and ending at "number of shadows"-1.
     * Strong shadows generally precede weak shadows in the index, and are grouped by their advice name.
     * @author Eric Bodden
	 */
	protected class ShadowGroupsRecord {
		
		/**
		 * A mapping from a shadow to its index.
		 */
		protected final Map<Shadow, Integer> shadowToIndex;
		
		/**
		 * The boolean matrix that we use to cache the "is compatible with" relation. 
		 */
		protected final boolean[][] shadowIsCompatibleWith;
				
		/**
		 * The number of all strong shadows for this dependency. 
		 */
		protected final int numStrongShadows;
		
		/**
		 * An array that stores for each shadow with index i its dependent advice name.
		 * (Stored here to avoid re-computation.) 
		 */
		protected final String[] dependentAdviceNames;
		
		/**
		 * Array mapping from a shadow's index to the shadow itself.
		 */
		protected final Shadow[] shadows;
		
		/**
		 * An array of BitSets where overlappingShadows[i] holds a bit <i>true</i>
		 * for each shadow that overlaps with shadow i.
		 */
		protected final BitSet[] overlappingShadows;
		
		/**
		 * Creates a new {@link ShadowGroupsRecord}.
		 * @param strong a set of strong advice
		 * @param weak a set of weak advice
		 */
		public ShadowGroupsRecord(Set<Shadow> strong, Set<Shadow> weak) {
			DAInfo dai = ((HasDAInfo)Main.v().getAbcExtension()).getDependentAdviceInfo();

			numStrongShadows = strong.size();
			
			//*HAS* to be a LinkedHashSet, because we want to make sure that strong shadows come first!
			Set<Shadow> domain = new LinkedHashSet<Shadow>(strong);
			domain.addAll(weak);
			
			/*
			 * Build up mappings etc.
			 */
			final int NUM = domain.size();
			int i=0;
			Shadow[] shadowList = new Shadow[NUM];
			dependentAdviceNames = new String[NUM];
			shadows = new Shadow[NUM];
			shadowToIndex = new HashMap<Shadow, Integer>();
			for (Shadow shadow : domain) {
				shadowList[i] = shadow;
				String depdendentAdviceName =
					dai.replaceForHumanReadableName(dai.qualifiedNameOfAdvice(shadow.getAdviceDecl()));
				dependentAdviceNames[i] = depdendentAdviceName;
				shadows[i] = shadow;
				shadowToIndex.put(shadow, i++);
			}
			
			/*
			 * Initialize ragged array.
			 */
			shadowIsCompatibleWith = new boolean[NUM][];
			for(i=0; i<NUM; i++) {
				shadowIsCompatibleWith[i] = new boolean[i+1];
			}		
			
			overlappingShadows = new BitSet[NUM];
			
			/*
			 * Fill array with computed values...
			 */
			for(i=0; i<NUM; i++) {
				for(int j=0; j<=i; j++) {
					boolean compatible;
					if(i==j) {
						//a shadow is always compatible to itself.
						compatible = true;
					} else {
						//get shadow at position i
						Shadow s1 = shadowList[i];
						//get shadow at position j
						Shadow s2 = shadowList[j];
						//compare
						compatible = comparator.compatibleBindings(s1, s2);
					}
					shadowIsCompatibleWith[i][j] = compatible;
				}
			}
			
		}
			
		/**
		 * For two shadows s1, s2 returns <code>true</code> if s1 is compatible with s2,
		 * according to this dependency.
		 * @see ShadowComparator#compatibleBindings(Shadow, Shadow)
		 */
		public boolean compatible(Shadow s1, Shadow s2) {
			Integer n1 = shadowToIndex.get(s1); 
			Integer n2 = shadowToIndex.get(s2);
			if(n1==null || n2==null) {
				throw new IllegalArgumentException("Shadow not part of this dependency!");
			}
			//make i1 the larger of the two indices
			int i1, i2;
			if(n1>n2){
				i1 = n1;
				i2 = n2;
			} else {
				i1 = n2;
				i2 = n1;
			}
			return shadowIsCompatibleWith[i1][i2];
		}
		
		/**
		 * Returns the value at position (j,i) of {@link #shadowIsCompatibleWith} if i<j, or
		 * the value at position (i,j) otherwise. Therefore, by accessing the array through this method,
		 * the array is virtually completed from a ragged array to a (mirrored) square array.
		 */
		protected boolean get(int i, int j) {
			if(i<j){
				//swap
				int z = j;
				j = i;
				i = z;
			} 
			return shadowIsCompatibleWith[i][j];
		}
		
		/**
		 * Returns <code>true</code> if the shadow s is kept alive by this dependency.
		 * This is the case if for each strong shadow name in the dependency there exists
		 * one shadow with that name that is (a) enabled and (b) compatible with s.
		 * We return <code>false</code> also in cases where s is not at all a shadow
		 * referred to by this dependency.
		 * @see Shadow#isEnabled() 
		 * @see ShadowComparator#compatibleBindings(Shadow, Shadow)
		 */
		protected boolean keepsShadowAlive(Shadow s) {
			Integer num = shadowToIndex.get(s);
			if(num==null) {
				return false;
			}
			int shadowNumber = num;
			
			/*
			 * We compute the result as follows. We can assume that shadows in the array are
			 * ordered such that (a) strong shadows precede weak shadows.
			 *
			 * Shadow s is kept alive, if in each such group there exists at least one such shadow in each group.
			 * Because of (a) it suffices to iterate up to numStrongShadows.
			 */
			 
			Set<String> strongNamesSeen = new HashSet<String>();
			
			for(int i=0;i<numStrongShadows;i++) {
				if(shadows[i].isEnabled() && get(shadowNumber,i)) {
					strongNamesSeen.add(dependentAdviceNames[i]);
				}
			}			
			
			return strongNamesSeen.containsAll(strongAdviceNameToVars.keySet());			
		}

		public Collection<Shadow> getOverlappingEnabledShadows(Shadow s) {
			Set<Shadow> res = new HashSet<Shadow>();
			//every shadow overlaps with itself
			res.add(s);
			
			Integer num = shadowToIndex.get(s);
			if(num==null) {
				//shadow is not even part of this dependency; no overlaps
				return res;
			}
			
			BitSet overlappingShadows = overlappingShadows(num);
			for(int i=0; i<shadows.length; i++) {
				if (overlappingShadows.get(i)) {
					Shadow shadow = shadows[i];
					if(shadow.isEnabled()) {
						res.add(shadow);
					}
				}
			}
			
			return res;
		}

		protected BitSet overlappingShadows(int num) {
			if(overlappingShadows[num]==null) {

				BitSet bitSet = new BitSet(shadows.length);
				//for all enabled shadows that overlap with s
				for(int i=0;i<shadows.length;i++) {
					Shadow otherShadow = shadows[i];
					if(get(num,i) && otherShadow.isEnabled()) {
						//fix this shadow "otherShadow";
						//now see if for the combination of s and otherShadow there
						//is for every strong symbol at least one shadow that
						//overlaps with both					
						
						Set<String> strongNamesSeen = new HashSet<String>();					
						for(int j=0;j<numStrongShadows;j++) {
							Shadow strongShadow = shadows[j];
							if(get(num,j) && get(i,j) && strongShadow.isEnabled()) {
								strongNamesSeen.add(dependentAdviceNames[j]);
							}
						}
						//if we have seen all necessary strong names
						if(strongNamesSeen.containsAll(strongAdviceNameToVars.keySet())) {
							bitSet.set(i);
						}
					}
				}
				overlappingShadows[num] = bitSet;
			}
			return overlappingShadows[num];
		}
		
	}
	
	
	public void computeConsistentShadowGroups(Set<Shadow> dependentAdviceShadows) {
		DAInfo dai = ((HasDAInfo)Main.v().getAbcExtension()).getDependentAdviceInfo();

		if(shadowGroupsRecord==null) {
		
			Set<String> strongAdviceNames = strongAdviceNameToVars.keySet();
			LinkedHashSet<Shadow> strongShadows = new LinkedHashSet<Shadow>();
			for (Shadow shadow : dependentAdviceShadows) {
				for (String adviceName : strongAdviceNames) {
					String depdendentAdviceName =
						dai.replaceForHumanReadableName(dai.qualifiedNameOfAdvice(shadow.getAdviceDecl()));
					if(shadow.declaringAspect().equals(container) && adviceName.equals(depdendentAdviceName)) {
						if(shadow.isEnabled())
							strongShadows.add(shadow);
					}					
				}				
			}
	
			Set<String> weakAdviceNames = weakAdviceNameToVars.keySet();
			LinkedHashSet<Shadow> weakShadows = new LinkedHashSet<Shadow>();
			for (Shadow shadow : dependentAdviceShadows) {
				for (String adviceName : weakAdviceNames) {
					String depdendentAdviceName =
						dai.replaceForHumanReadableName(dai.qualifiedNameOfAdvice(shadow.getAdviceDecl()));
					if(shadow.declaringAspect().equals(container) && adviceName.equals(depdendentAdviceName)) {
						if(shadow.isEnabled())
							weakShadows.add(shadow);
					}					
				}				
			}
			
			weakShadows.removeAll(strongShadows);

			shadowGroupsRecord = new ShadowGroupsRecord(strongShadows,weakShadows);
		} 
	}
	
	public static Bag<AdviceDecl> disableShadowsWithNoStrongSupportByAnyGroup(Set<Shadow> dependentAdviceShadows) {
		DAInfo dai = ((HasDAInfo)Main.v().getAbcExtension()).getDependentAdviceInfo();

		Set<AdviceDependency> dependencies = dependenciesPassingQuickCheck();
		
		Bag<AdviceDecl> disabledShadowsPerAdvice = new HashBag<AdviceDecl>();
		
		for (Shadow shadow : dependentAdviceShadows) {
			if(dai.isDependentAdvice(shadow.getAdviceDecl())) {
				boolean isAlive = false;
				for (AdviceDependency dependency : dependencies) {
					if(dependency.keepsShadowAlive(shadow)) {
						isAlive = true;
						break;
					}
				}
				if(!isAlive && shadow.isEnabled()) {					
					shadow.disable();
					disabledShadowsPerAdvice.add(shadow.getAdviceDecl());
					
					if(OptionsParser.v().warn_about_individual_shadows()) {
						((HasDAInfo)Main.v().getAbcExtension()).flowInsensitiveAnalysis().warn(
								shadow,
								"Orphan shadow disabled by flow-insensitive analysis."
						);
					}
				}
			}
		}

		for (AdviceDependency dependency : dependencies) {
			dependency.invalidateCache();
		}

		return disabledShadowsPerAdvice;
	}

	/**
	 * Invalidates the cache that is used when computing overlapping shadows.
	 */
	protected void invalidateCache() {
		if(shadowGroupsRecord!=null) {
			for(int i=0;i<shadowGroupsRecord.overlappingShadows.length;i++) {
				shadowGroupsRecord.overlappingShadows[i] = null;
			}
		}
	}

	protected static Set<AdviceDependency> dependenciesPassingQuickCheck() {
		DAInfo dai = ((HasDAInfo)Main.v().getAbcExtension()).getDependentAdviceInfo();
		Set<AdviceDependency> dependencies = new HashSet<AdviceDependency>(dai.getAdviceDependencies());
		//prune dependencies that already fail the quick check; those we don't care about
		for (Iterator<AdviceDependency> depIter = dependencies.iterator(); depIter.hasNext();) {
			AdviceDependency ad = (AdviceDependency) depIter.next();
			if(!ad.fulfillsQuickCheck()) {
				depIter.remove();
			}
		}
		return dependencies;
	}
	
	public static Set<Shadow> getAllEnabledShadowsOverlappingWith(Collection<Shadow> shadows) {
		DAInfo dai = ((HasDAInfo)Main.v().getAbcExtension()).getDependentAdviceInfo();
		
		Set<Shadow> overlappingShadows = new HashSet<Shadow>();
		Set<AdviceDependency> dependencies = dependenciesPassingQuickCheck();
		
		for (Shadow shadow : shadows) {
			if(dai.isDependentAdvice(shadow.getAdviceDecl())) {
				for (AdviceDependency dependency : dependencies) {
					overlappingShadows.addAll(dependency.getOverlappingEnabledShadows(shadow));
				}
			}
		}
		
		return overlappingShadows;
	}

	protected Collection<Shadow> getOverlappingEnabledShadows(Shadow shadow) {
		if(shadowGroupsRecord==null) {
			throw new IllegalStateException("Shadow groups have not yet been computed.");			
		}
		return shadowGroupsRecord.getOverlappingEnabledShadows(shadow);
	}

}
