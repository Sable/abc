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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.Position;
import soot.SootMethod;
import abc.da.ast.DAAdviceDecl;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.da.weaving.weaver.depadviceopt.ds.ShadowComparator;
import abc.main.Main;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
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
	 * The cached set of consistent shadow groups.
	 */
	protected Set<DependentShadowGroup> consistentShadowGroups;

	protected FixedUniverse<Shadow> fixedUniverse;

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
		final DAGlobalAspectInfo gai = (DAGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {

			public void adviceApplication(AdviceApplication aa, SootMethod m) {
				boolean isDependent = aa.advice.getFlags().intersects(DAAdviceDecl.DEPENDENT);
				if(aa.advice.getAspect().equals(container) && isDependent && aa.advice.getApplCount()>0) {
						//found a matched dependent advice; remove it from the set of strong advice
						strongAdviceNamesThatDidNotMatch.remove(gai.replaceForHumanReadableName(aa.advice.getQualifiedAdviceName()));					
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
	 * Returns <code>true</code> if this depencency contains an advice with the given name.
	 */
	public boolean containsAdviceNamed(String adviceName) {
		return strongAdviceNameToVars.keySet().contains(adviceName) ||
		       weakAdviceNameToVars.keySet().contains(adviceName);
	}

	/**
	 * Computes, respectively re-computes all consistent shadow groups of this advice dependency.
	 */
	public Set<DependentShadowGroup> computeOrUpdateConsistentShadowGroups(Set<Shadow> dependentAdviceShadows) {
		
		fixedUniverse = new FixedUniverse<Shadow>(dependentAdviceShadows);
		
		if(consistentShadowGroups==null) {
			Map<String,Set<Shadow>> adviceNameToShadows = new HashMap<String,Set<Shadow>>();
			for (String adviceName : adviceNames()) {
				Set<Shadow> shadows = fixedUniverse.newSet();
				adviceNameToShadows.put(adviceName,shadows);
				for (Shadow shadow : dependentAdviceShadows) {
					assert shadow.belongsToDependentAdvice();
					if(shadow.declaringAspect().equals(container) && adviceName.equals(shadow.dependentAdviceName())) {
						shadows.add(shadow);
					}
				}
			}

			List<Set<Shadow>> setsOfStrongShadows = new LinkedList<Set<Shadow>>();
			for (String strongAdviceName : strongAdviceNameToVars.keySet()) {
				setsOfStrongShadows.add(adviceNameToShadows.get(strongAdviceName));
			}
			List<Set<Shadow>> setsOfWeakShadows = new LinkedList<Set<Shadow>>();
			for (String weakAdviceName : weakAdviceNameToVars.keySet()) {
				setsOfWeakShadows.add(adviceNameToShadows.get(weakAdviceName));
			}
			
			//cache the groups
			consistentShadowGroups = consistentCrossProduct(setsOfStrongShadows, setsOfWeakShadows);
		} else {
			//remove all shadow groups for which a strong shadow has been disabled in the meantime
			for (Iterator<DependentShadowGroup> iterator = consistentShadowGroups.iterator(); iterator.hasNext();) {
				DependentShadowGroup shadowGroup = iterator.next();
				for (Shadow shadow : shadowGroup.strongShadows) {
					if(!shadow.isEnabled()) {
						iterator.remove();
					}
				}
			}
		}
		
		return new HashSet<DependentShadowGroup>(consistentShadowGroups);
	}
	
	
	/**
	 * Computes a consistent cross product of the given sets of strong and weak shadows.
	 */
	protected Set<DependentShadowGroup> consistentCrossProduct(Collection<Set<Shadow>> toCrossStrong, Collection<Set<Shadow>> toCrossWeak) {
		HashSet<DependentShadowGroup> result = new HashSet<DependentShadowGroup>();
		DependentShadowGroup seed = new DependentShadowGroup();
		result.add(seed);
		
		for (Set<Shadow> currSet : toCrossStrong) {			
			result = singleProduct(currSet, result, true);			
		}
		for (Set<Shadow> currSet : toCrossWeak) {			
			result = singleProduct(currSet, result, false);			
		}

		//remove the empty group as it was only used as a seed
		result.remove(seed);
		
		return result;
	}

	private HashSet singleProduct(Set<Shadow> currSet, HashSet<DependentShadowGroup> result, boolean strongShadows) {
		HashSet newResult = new HashSet();
		
		for (DependentShadowGroup resGroup : result) {
			
			for (Shadow shadow : currSet) {
				
				DependentShadowGroup currCopy = resGroup.clone(); 

				//TODO document
				if(strongShadows && currCopy.tryAddStrongShadow(shadow)) {
					newResult.add(currCopy);
				} else if(!strongShadows) {
					currCopy.tryAddWeakShadow(shadow);
					newResult.add(currCopy);
				}
			}
		}
		return newResult;
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
	
	
	/**
	 * A group of dependent shadows, all shadows only have to execute
	 * if the strong shadows in the group may execute.
	 * @author Eric Bodden
	 */
	public class DependentShadowGroup implements Cloneable {
		
		protected FixedUniverse<Shadow>.FixedUniverseSet strongShadows;

		protected FixedUniverse<Shadow>.FixedUniverseSet weakShadows;
		
		public DependentShadowGroup() {
			strongShadows = fixedUniverse.newSet();
			weakShadows = fixedUniverse.newSet();
		}
		
		/**
		 * Tries to add a strong shadow to the group.
		 * The shadow is only added if its points-to sets overlap with
		 * all other shadows in the group on the variables that were
		 * stated in the dependency declaration.
		 * Returns <code>true</code> if the shadow was added. 
		 */
		public boolean tryAddStrongShadow(Shadow newShadow) {
			return tryAddShadow(newShadow, strongShadows);
		}
		
		/**
		 * Tries to add a weak shadow to the group.
		 * The shadow is only added if its points-to sets overlap with
		 * all other shadows in the group on the variables that were
		 * stated in the dependency declaration.
		 * Returns <code>true</code> if the shadow was added. 
		 */
		public boolean tryAddWeakShadow(Shadow newShadow) {
			return tryAddShadow(newShadow, weakShadows);
		}
		
		protected boolean tryAddShadow(Shadow newShadow, Set<Shadow> setToAddTo) {
			for (Shadow s : strongShadows) {
				if(!comparator.compatibleBindings(s, newShadow)) {
					return false;
				}
			}
			for (Shadow s : weakShadows) {
				if(!comparator.compatibleBindings(s, newShadow)) {
					return false;
				}
			}
			setToAddTo.add(newShadow);
			return true;
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("=================================================================");
			sb.append("Strong shadows:\n\n");
			for (Shadow s : strongShadows) {
				sb.append(s.toString());
				sb.append("\n");
				sb.append("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
			}
			sb.append("=================================================================");
			sb.append("Weak shadows:\n\n");
			for (Shadow s : weakShadows) {
				sb.append(s.toString());
				sb.append("\n");
				sb.append("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
			}
			sb.append("=================================================================");
			return super.toString();
		}
		
		@Override
		public DependentShadowGroup clone() {
			try {
				DependentShadowGroup clone = (DependentShadowGroup) super.clone();
				clone.strongShadows = strongShadows.clone();
				clone.weakShadows = weakShadows.clone();
				return clone;
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((strongShadows == null) ? 0 : strongShadows.hashCode());
			result = prime * result
					+ ((weakShadows == null) ? 0 : weakShadows.hashCode());
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
			DependentShadowGroup other = (DependentShadowGroup) obj;
			if (strongShadows == null) {
				if (other.strongShadows != null)
					return false;
			} else if (!strongShadows.equals(other.strongShadows))
				return false;
			if (weakShadows == null) {
				if (other.weakShadows != null)
					return false;
			} else if (!weakShadows.equals(other.weakShadows))
				return false;
			return true;
		}

		/**
		 * Returns the set of all shadows in this group.
		 */
		public Set<Shadow> allShadows() {
			Set<Shadow> allShadows = new HashSet<Shadow>();
			allShadows.addAll(strongShadows);
			allShadows.addAll(weakShadows);
			return allShadows;
		}
		
	}

	

}
