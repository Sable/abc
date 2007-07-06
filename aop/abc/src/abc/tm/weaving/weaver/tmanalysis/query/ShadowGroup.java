/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import soot.jimple.toolkits.pointer.FullObjectSet;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.ds.Intersection;
import abc.tm.weaving.weaver.tmanalysis.query.ConsistentShadowGroupFinder.ConsistentShadowBag;

/**
 * A shadow group is a group of shadows with a consistent vriable binding.
 *
 * @author Eric Bodden
 */
public class ShadowGroup {
	
	protected final Set<SymbolShadowWithPTS> labelShadows;
	
	protected final Set<SymbolShadowWithPTS> skipShadows;
	
	protected final TraceMatch tm;
	
    /** mapping of variables to points-to sets; needed to narrow down
	 *  points-to sets during inlining */
	protected final HashMap<String,PointsToSet> varToPts;
	
	protected final int number;

    private final int hashCode;
	
	protected static int groupCount = 0;
	
    protected transient Set<SymbolShadowWithPTS> allShadows;

    public ShadowGroup(TraceMatch tm, ConsistentShadowBag shadowSet) {
		this.tm = tm;
		this.labelShadows = new HashSet<SymbolShadowWithPTS>();
		this.varToPts = new HashMap<String, PointsToSet>();
		for (Iterator iterator = shadowSet.iterator(); iterator.hasNext();) {
			SymbolShadowWithPTS shadow = (SymbolShadowWithPTS) iterator.next();
			add(shadow);
		}
		this.skipShadows = new HashSet<SymbolShadowWithPTS>();
        this.allShadows = new HashSet<SymbolShadowWithPTS>();
		this.number = groupCount;
		groupCount++;
		this.hashCode = computeHashCode();
	}
	
	/**
	 * Adds the shadow o to this component, but only if its points-to set has a non-empty
	 * intersection with the sets of the shadows contained already in this component.
	 * @param o any {@link SymbolShadowWithPTS} 
	 */
	protected boolean add(SymbolShadowWithPTS s) {
		//TODO high code duplication with ConsistentShadowSet!
		Map<String,PointsToSet> newVarToPts = new HashMap();
		for (Iterator varIter = s.getBoundTmFormals().iterator(); varIter.hasNext();) {
			String var = (String) varIter.next();
			
			PointsToSet thisPts = (PointsToSet) varToPts.get(var);
			if(thisPts==null) thisPts = FullObjectSet.v();
			
			PointsToSet othPts = s.getPointsToSetForVariable(var);  
			if(othPts==null) othPts = FullObjectSet.v();
			
			PointsToSet intersection = Intersection.intersect(thisPts, othPts);
			if(intersection.isEmpty()) {
				//cannot add this shadow, because the points-to sets are
				//not intersecting and hence no consistent binding is possible
				throw new IllegalArgumentException("shadow set has no overlapping variable binding!");
			} else {
				//store intersectionhashCode
				newVarToPts.put(var,intersection);					
			}
		}
		
		//if we came here, this means we had intersecting points-to sets for all variables 
		//so store the intersection and add the shadow to the set
		
		//store the intersections
		varToPts.putAll(newVarToPts);
		
		//reset cache
		this.allShadows = null;
		
		//commit the shadow
		return labelShadows.add(s);
	}
	
	protected boolean hasNonEmptyIntersection(SymbolShadowWithPTS s) {
		for (Iterator varIter = s.getBoundTmFormals().iterator(); varIter.hasNext();) {
			String var = (String) varIter.next();
			
			PointsToSet thisPts = (PointsToSet) varToPts.get(var);
			if(thisPts==null) thisPts = FullObjectSet.v();
			
			PointsToSet othPts = s.getPointsToSetForVariable(var);  
			if(othPts==null) othPts = FullObjectSet.v();
			
			PointsToSet intersection = Intersection.intersect(thisPts, othPts);
			if(intersection.isEmpty()) {
				//cannot add this shadow, because the points-to sets are
				//not intersecting and hence no consistent binding is possible
				return false;
			} 
		}
		return true;			
	}
	
	public boolean addSkipShadow(SymbolShadowWithPTS s) {
		if(hasNonEmptyIntersection(s)) {
	        //reset cache
	        this.allShadows = null;
			return skipShadows.add(s);
		} else {
			return false;
		}
	}

	/**
	 * @return the labelShadows
	 */
	public Set getLabelShadows() {
		return Collections.unmodifiableSet(labelShadows);
	}

	/**
	 * @return the skipShadows
	 */
	public Set getSkipShadows() {
		return Collections.unmodifiableSet(skipShadows);
	}

	/**
	 * @return the tm
	 */
	public TraceMatch getTraceMatch() {
		return tm;
	}
	
	public Set<SymbolShadowWithPTS> getAllShadows() {
	    if(allShadows==null) {
        	allShadows = new HashSet(labelShadows);
        	allShadows.addAll(skipShadows);
	    }
		return allShadows;		
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
	    return hashCode;
	}
	
	/**
	 * Computes the constant hash code.
	 */
	private int computeHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((labelShadows == null) ? 0 : labelShadows.hashCode());
		result = prime * result
				+ ((skipShadows == null) ? 0 : skipShadows.hashCode());
		result = prime * result + ((tm == null) ? 0 : tm.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ShadowGroup other = (ShadowGroup) obj;
		if (labelShadows == null) {
			if (other.labelShadows != null)
				return false;
		} else if (!labelShadows.equals(other.labelShadows))
			return false;
		if (skipShadows == null) {
			if (other.skipShadows != null)
				return false;
		} else if (!skipShadows.equals(other.skipShadows))
			return false;
		if (tm == null) {
			if (other.tm != null)
				return false;
		} else if (!tm.equals(other.tm))
			return false;
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "tracematch: "+tm.getName()+"\nnormal shadows: "+labelShadows + "\nskip shadows: "+skipShadows; 
	}

	/**
	 * @return the unique number of this group
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Returns <code>true</code> if this variable binding has an overlapping
	 * points-to set with the bindings in this group.
	 * @param tmVar a tracematch variable
	 * @param pts a variable binding modeled by a points-to set
	 */
	public boolean hasCompatibleBinding(String tmVar, PointsToSet pts) {
		PointsToSet storedPts = varToPts.get(tmVar);
		if(storedPts==null) {
			throw new RuntimeException("This shadow group seems to have no binding for "+tmVar+"!\n"+toString());			
		} else {
			return storedPts.hasNonEmptyIntersection(pts);
		}
	}
	
}
