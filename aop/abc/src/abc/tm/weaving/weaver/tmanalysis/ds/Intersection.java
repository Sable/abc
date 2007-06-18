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
package abc.tm.weaving.weaver.tmanalysis.ds;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.PointsToSet;
import soot.jimple.spark.sets.EmptyPointsToSet;
import soot.jimple.toolkits.pointer.FullObjectSet;

/**
 * Implements an intersection of two points-to sets without altering them.
 * Currently only work with Spark.
 *
 * @author Eric Bodden
 */
public class Intersection implements PointsToSet {

	public static int maxDepth = 0;
	
	/** The two sets. */
	protected PointsToSet s1, s2;
	
	/** set representation for easier equality check */
	private Set asSet;
	
	/** Indicates whether this intersection is empty. */
	private final boolean isEmpty;
	
    /**
     * Creates an intersection of <code>s1</code> and <code>s2</code>.
	 * @param s1 the set to intersect with <code>s2</code>
	 * @param s2 the set to intersect with <code>s1</code>
	 */
	protected Intersection(PointsToSet s1, PointsToSet s2) {
		assert s1.hasNonEmptyIntersection(s2);
//		if(s1 instanceof PointsToSetReadOnly) {
//			s1 = new PaddlePointsToSetCompatibilityWrapper((PointsToSetReadOnly)s1);
//			new RuntimeException("Internal error").printStackTrace();
//		}
//		if(s2 instanceof PointsToSetReadOnly) {
//			s2 = new PaddlePointsToSetCompatibilityWrapper((PointsToSetReadOnly)s2);
//			new RuntimeException("Internal error").printStackTrace();
//		}
		this.s1 = s1;
		this.s2 = s2;
		
		this.asSet = new HashSet(2);
		this.asSet.add(this.s1);
		this.asSet.add(this.s2);
		
		//the intersection is empty if either of the two sets are empty
		isEmpty = s1==null || s2==null || s1.isEmpty() || s2.isEmpty();
		
		int depth = this.depth();
		if(depth>maxDepth) maxDepth = depth;
	}

    /**
     * {@inheritDoc}
     */
    public Set possibleStringConstants() { return null; }
    
    /**
     * {@inheritDoc}
     */
    public Set possibleClassConstants() { return null; }

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNonEmptyIntersection(PointsToSet s3) {
		if(isEmpty()) {
			return false;
		}
		if(s1==null||s2==null||s3==null) {
			//if any of the sets is null, we certainly have an empty
			//intersection
			return false;
		} else {
			//otherwise, we have to check pairwise (set intersection is not
			//transitive)
			return /*s1.hasNonEmptyIntersection(s2) &&*/ // this is true by construction
				s1.hasNonEmptyIntersection(s3) &&				
				s2.hasNonEmptyIntersection(s3);				
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return isEmpty;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set possibleTypes() {
		if (s1==null||s2==null) {
			return Collections.EMPTY_SET;
		} else {
			//intersect the possible types if s1 and s2
			Set ret = s1.possibleTypes();
			ret.retainAll(s2.possibleTypes());
			return ret;
		}		
	}
	
	/**
	 * Intersects p1 and p2.
	 * Returns an object of type {@link PointsToSet} but not necessarily {@link Intersection}.
	 */
	public static PointsToSet intersect(PointsToSet p1, PointsToSet p2) {
		if(p1==null || p2==null || p1==EmptyPointsToSet.v() || p2==EmptyPointsToSet.v()) {
			//if either is empty, the intersection is empty
			return EmptyPointsToSet.v();
		} else {
			//if either is the full set, just return the other one
			if(p1==FullObjectSet.v()) {
				return p2;
			}
			if(p2==FullObjectSet.v()) {
				return p1;
			}
			if(p1 instanceof Intersection) {
				Intersection i1 = (Intersection) p1; 
				if(i1.contains(p2)) {
					return i1;
				}
			}
			if(p2 instanceof Intersection) {
				Intersection i2 = (Intersection) p2; 
				if(i2.contains(p1)) {
					return i2;
				}
			} 
			if(p1.equals(p2)) {
				return p1;
			}
			if(!p1.hasNonEmptyIntersection(p2)) {
				return EmptyPointsToSet.v();
			}
			//else create a new Intersection
			return new Intersection(p1,p2);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((asSet == null) ? 0 : asSet.hashCode());
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
		final Intersection other = (Intersection) obj;
		if (asSet == null) {
			if (other.asSet != null)
				return false;
		} else if (!asSet.equals(other.asSet))
			return false;
		return true;
	}
	
	public int depth() {
		int left = 0, right = 0;
		if(s1 instanceof Intersection) {
			Intersection is = (Intersection) s1;
			left = is.depth();
		} 
		if(s2 instanceof Intersection) {
			Intersection is = (Intersection) s2;
			right = is.depth();
		} 
		return Math.max(left, right)+1;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "<" + s1 + " && " + s2 +">";
	}
	
	public boolean contains(PointsToSet pts) {
		if(s1.equals(pts)) {
			return true;
		}
		if(s2.equals(pts)) {
			return true;
		}
		if(s1 instanceof Intersection) {
			Intersection i1 = (Intersection) s1;
			if(i1.contains(pts)) {
				return true;
			}
		}
		if(s2 instanceof Intersection) {
			Intersection i2 = (Intersection) s2;
			if(i2.contains(pts)) {
				return true;
			}
		}
		return false;
	}

}

