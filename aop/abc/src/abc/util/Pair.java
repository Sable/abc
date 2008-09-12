/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Damien Sereni
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

package abc.util;

/**
 * An (immutable) ordered pair of values.
 * 
 * <p>Pairs are compared with structural equality: <code>(x,y) = (x', y')</code>
 * iff <code>x=x'</code> and <code>y=y'</code>.</p>
 * 
 * @author Damien Sereni
 * @param <X> the type of the first component of the pair
 * @param <Y> the type of the second component of the pair
 *
 */
public class Pair<X,Y> {

	private X x;
	private Y y;
	
	/*
	 * Constructor and factory
	 */
	
	/**
	 * Create a new pair of values
	 * @param x the first component of the pair
	 * @param y the second component of the pair
	 */
	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Create a new pair of values. This behaves identically
	 * to the constructor, but benefits from type inference
	 * @param x the first component of the pair
	 * @param y the second component of the pair
	 */
	public static <X,Y> Pair<X,Y> make(X x, Y y) {
		return new Pair<X,Y>(x, y);
	}
	
	/*
	 * Getters
	 */
	
	/**
	 * Get the first component of this pair
	 * @return the first component of the pair
	 */
	public X fst() {
		return x;
	}
	
	/**
	 * Get the second component of this pair
	 * @return the second component of the pair
	 */
	public Y snd() {
		return y;
	}

	/*
	 * Equality and hashing
	 */
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((x == null) ? 0 : x.hashCode());
		result = PRIME * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Pair other = (Pair) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}
	
	/*
	 * Printing
	 */
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
}
