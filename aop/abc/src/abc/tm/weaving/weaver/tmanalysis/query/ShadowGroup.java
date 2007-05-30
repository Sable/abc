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
import java.util.Map.Entry;

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
	
	protected Set labelShadows;
	
	protected Set skipShadows;
	
	protected TraceMatch tm;
	
	/** mapping of variables to points-to sets; needed to narrow down
	 *  points-to sets during inlining */
	protected HashMap varToPts;
	
	protected int number;
	
	protected static int groupCount = 0;
	
	public ShadowGroup(TraceMatch tm, ConsistentShadowBag shadowSet) {
		this.tm = tm;
		this.labelShadows = new HashSet();
		this.varToPts = new HashMap();
		for (Iterator iterator = shadowSet.iterator(); iterator.hasNext();) {
			Shadow shadow = (Shadow) iterator.next();
			add(shadow);
		}
		this.skipShadows = new HashSet();
		this.number = groupCount;
		groupCount++;
	}
	
	/**
	 * Adds the shadow o to this component, but only if its points-to set has a non-empty
	 * intersection with the sets of the shadows contained already in this component.
	 * @param o any {@link Shadow} 
	 */
	protected boolean add(Shadow s) {
		//TODO high code duplication with ConsistentShadowSet!
		Map newVarToPts = new HashMap();
		for (Iterator varIter = s.getBoundVariables().iterator(); varIter.hasNext();) {
			String var = (String) varIter.next();
			
			PointsToSet thisPts = (PointsToSet) varToPts.get(var);
			if(thisPts==null) thisPts = FullObjectSet.v();
			
			PointsToSet othPts = s.getPointsToSet(var);  
			if(othPts==null) othPts = FullObjectSet.v();
			
			PointsToSet intersection = Intersection.intersect(thisPts, othPts);
			if(intersection.isEmpty()) {
				//cannot add this shadow, because the points-to sets are
				//not intersecting and hence no consistent binding is possible
				throw new IllegalArgumentException("shadow set has no overlapping variable binding!");
			} else {
				//store intersection
				newVarToPts.put(var,intersection);					
			}
		}
		
		//if we came here, this means we had intersecting points-to sets for all variables 
		//so store the intersection and add the shadow to the set
		
		//store the intersections
		for (Iterator intIter = newVarToPts.entrySet().iterator(); intIter.hasNext();) {
			Entry entry = (Entry) intIter.next();
			varToPts.put(entry.getKey(), entry.getValue());
		}
		
		//commit the shadow
		return labelShadows.add(s);
	}
	
	protected boolean hasNonEmptyIntersection(Shadow s) {
		for (Iterator varIter = s.getBoundVariables().iterator(); varIter.hasNext();) {
			String var = (String) varIter.next();
			
			PointsToSet thisPts = (PointsToSet) varToPts.get(var);
			if(thisPts==null) thisPts = FullObjectSet.v();
			
			PointsToSet othPts = s.getPointsToSet(var);  
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
	
	public boolean addSkipShadow(Shadow s) {
		if(hasNonEmptyIntersection(s))
			return skipShadows.add(s);
		else
			return false;
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
	
	public Set getAllShadows() {
		Set result = new HashSet(labelShadows);
		result.addAll(skipShadows);
		return result;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
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
	
	
}
