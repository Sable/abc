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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import soot.util.IdentityHashSet;
import abc.da.weaving.aspectinfo.FixedUniverse;
import abc.da.weaving.aspectinfo.FixedUniverse.FixedUniverseMap;

/**
 * A {@link Bag} that uses a {@link HashMap} as backing map.
 * @author Eric Bodden
 */
public class HashBag<E> extends AbstractCollection<E> implements Bag<E>, Cloneable {

	/**
	 * An iterator for a {@link HashBag}.
	 * The iterator returns an object o n times if o is contained n times in the bag.
	 * <b>This iterator does not implement fast-fail semantics!</b>
	 * @author Eric Bodden
	 */
	protected class HashBagIterator implements Iterator<E> {

		protected Iterator<Entry<E, Integer>> backingMapIterator;
		protected Map.Entry<E,Integer> currentEntry;		
		protected int currentCount;
		protected E lastObject;
		
		protected HashBagIterator() {
		    Set<Entry<E, Integer>> entrySet = backingMap.entrySet();
            Set<Entry<E, Integer>> filteredEntrySet;
		    if(backingMap instanceof FixedUniverseMap) {    		    
    		    //only retain all entries with a value > 0
		        filteredEntrySet = new IdentityHashSet<Entry<E,Integer>>();
                for (Entry<E, Integer> entry : entrySet) {
                    if(entry.getValue()>0) {
                        filteredEntrySet.add(entry);
                    }
                }
		    } else {
		        //for normal hash maps as backing map, such entries are directly removed
		        //(see remove(Object))
		        filteredEntrySet = entrySet;
		    }
			backingMapIterator = filteredEntrySet.iterator();
		}
		
		/**
		 * @inheritDoc
		 */
		public boolean hasNext() {
			if(currentEntry==null) {
				return init();
			} else {
				return currentCount <= currentEntry.getValue() ||
				       backingMapIterator.hasNext();
			}
		}

		/**
		 * @inheritDoc
		 */
		public E next() {
			if(!hasNext()) throw new NoSuchElementException();

			lastObject = currentEntry.getKey();
			
			if(currentCount < currentEntry.getValue()) {
				currentCount++;
				return currentEntry.getKey();
			} else {
				currentCount++;
				E currentObject = currentEntry.getKey();
				if(backingMapIterator.hasNext()) {
					currentEntry = backingMapIterator.next();
					currentCount = 1;
				}
				return currentObject;
			}
			
		}
		
		/**
		 * Initializes the iterator.
		 */
		protected boolean init() {
			if(currentEntry==null) {
				if(!backingMapIterator.hasNext()) return false;
				currentEntry = backingMapIterator.next();
				currentCount = 1;
			}
			return true;
		}

		/**
		 * @inheritDoc
		 */
		public void remove() {
			if(HashBag.this.remove(lastObject)) {
				currentCount--;
			}
		}

	}

	protected Map<E,Integer> backingMap;
	
	protected int size;
	
	/**
	 * Constructs a new, empty hash bag.
	 */
	@SuppressWarnings("serial")
	public HashBag() {
		backingMap = new HashMap<E,Integer>() {
			@Override
			public Integer get(Object key) {
				//return 0 as default
				Integer integer = super.get(key);
				if(integer==null) integer = 0;
				return integer;
			}
		};
		size = 0;
	}
	
    /**
     * Constructs a new hash bag where each element in c is contained once.
     */
	public HashBag(Collection<E> c) {
		this();
		addAll(c);
	}
	
    /**
     * Constructs a new, empty hash bag over values of the given fixed universe.
     * This is generally more efficient than using {@link #HashBag()} in cases
     * where multiple HashBags exist over the same values.
     */
	public HashBag(FixedUniverse<E> fixedUniverse) {
	    backingMap = fixedUniverse.newMap();
	    size = 0;
	}
	
	/** 
	 * Adds <i>o</i> to the bag.
	 * @param o any object
	 * @return true 
	 */
	public boolean add(E o) {
		int count = backingMap.get(o);
		int newCount = count+1;
		backingMap.put(o, newCount);
		size++;
		return true;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		int count = backingMap.get(o);
		if(count==0) {
			return false;
		} else {
			//..anyway, if we get here we know that o must be of type E because it's contained in the map 
			int newCount = count-1;
			if(newCount==0) {
			    if(backingMap instanceof HashMap) {
			        //actually remove the mapping to save space
			        backingMap.remove(o);
			    }			    
			} else {
				backingMap.put((E)o,newCount);
			}
			size--;
			return true;
		}		
	}
	
	
	/** 
	 * Returns an iterator for this bag.
	 * The iterator returns an object o n times if o is contained n times in the bag.
	 */
	public Iterator<E> iterator() {
		return new HashBagIterator();
	}

	/** 
	 * Returns the size, i.e. the number of all objects help in this bag
	 * where multiple instances of the same object are counted multiple times.
	 */
	public int size() {
		return size;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return backingMap.isEmpty();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void clear() {
		backingMap.clear();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return backingMap.hashCode();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		return backingMap.equals(obj);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public String toString() {
		return backingMap.toString();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	protected HashBag<E> clone() {
		try {
			HashBag<E> clone = (HashBag<E>) super.clone();
			if(backingMap instanceof HashMap) {
	            clone.backingMap = (HashMap<E, Integer>) ((HashMap)backingMap).clone();
			} else {
                clone.backingMap = (FixedUniverseMap) ((FixedUniverseMap)backingMap).clone();
			}
			
			return clone;
		} catch (CloneNotSupportedException e) {
			//cannot occur
			throw new RuntimeException();
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public int countOf(E o) {
		Integer val = backingMap.get(o);
		if(val==null) {
			return 0;
		} else {
			return val;
		}
	}
		
//	public static void main(String[] args) {
//		HashBag<String> b = new HashBag<String>();
//		b.add("a");
//		b.add("a");
//		b.add("a");
//		b.add("b");
//		b.remove("a");
//		
//		System.err.println(b.size());
//		
//		System.err.println(b);
//	
//		for (String string : b) {
//			System.err.println(string);
//		}
//		
//		for (Iterator iterator = b.iterator(); iterator.hasNext();) {
//			iterator.next();
//			iterator.remove();			
//		}
//		System.err.println(b.isEmpty());
//
//		b.add("a");
//		System.err.println(b.isEmpty());
//
//		b.clear();
//		System.err.println(b.isEmpty());
//	}
	
}
