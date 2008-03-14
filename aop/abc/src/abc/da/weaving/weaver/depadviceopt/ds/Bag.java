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
package abc.da.weaving.weaver.depadviceopt.ds;

import java.util.Collection;
import java.util.Iterator;

/**
 * A bag is a collection that is similar to a set but can hold the same object multiple times.
 * 
 * @author Eric Bodden
 */
public interface Bag<E> extends Collection<E> {

	/** 
	 * Adds <i>o</i> to the bag.
	 * @param o any object
	 * @return true 
	 */
	public boolean add(E o);
	
	/** 
	 * Returns an iterator for this bag.
	 * The iterator returns an object o n times if o is contained n times in the bag.
	 */
	public Iterator<E> iterator();
	
	/**
	 * Returns how often o is contained in this bag.
	 */
	public int countOf(E o);
	
}
