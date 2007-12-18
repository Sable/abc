/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Pavel Avgustinov
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

package org.aspectbench.tm.runtime.internal;

/**
 * Part of the Indexing Data Structure implementation. This is a HashMap-like
 * data structure that uses key identity as the basis for comparison. It isn't
 * quite conformant to the java.util.Map interface, since its hasNext() method
 * is (by design) unreliable. If it returns true, next() should be called and the return
 * value checked for non-nullness.
 * 
 * Only the methods of the Map interface that are required by the IDS are
 * implemented, all others just throw an exception.
 * 
 * @author Pavel Avgustinov
 */

import java.lang.ref.Reference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.aspectbench.tm.runtime.internal.WeakKeyCollectingIdentityHashMap.WeakKeyHashEntry;

public class IdentityHashMap implements Map {
    /** The default capacity to use */
    protected static final int DEFAULT_CAPACITY = 16;
    /** The default threshold to use */
    protected static final int DEFAULT_THRESHOLD = 12;
    /** The default load factor to use */
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /** The maximum capacity allowed */
    protected static final int MAXIMUM_CAPACITY = 1 << 30;

    /** Load factor, normally 0.75 */
    protected transient float loadFactor;
    /** The size of the map */
    protected transient int size = 0;
    /** Map entries */
    protected transient HashEntry[] data;
    /** Size at which to rehash */
    protected transient int threshold;
    /** Modification count for iterators */
    protected transient int modCount = 0;

    private static void notImplemented(String s) {
    	throw new RuntimeException("Method " + s + " is not implemented and shouldn't be required. Submit a stack trace if you think you need it.");
    }
    
    /**
     * An entry in the IdentityHashMap. Caches the hashCode (not really needed atm
     * I think), and holds references to a key-value pair.
     * 
     * Note that HashEntry.key is private --- it should be replaced by keys of the
     * appropriate types by subclasses.
     * 
     * @author Pavel Avgustinov
     */
    class HashEntry implements java.util.Map.Entry {
		// Next element in hash chain
    	protected HashEntry next;
    	// Cached hash code
    	protected int hashCode;
    	// Key -- may have to be a weakref!
    	private Object key;
    	// Object stored
    	protected Object value;
    	// Indicates whether the entry is "live" -- i.e. still in the HashMap.
    	// Should be set to false if it is safely removed from the map.
    	protected boolean live = true;
    	
    	protected HashEntry(HashEntry next, Object key, int hashCode, Object value) {
    		this.next = next;
    		this.key = key;
    		this.hashCode = hashCode;
    		this.value = value;
    	}
    	
    	// Don't take the key (so subclasses can wrap it)
    	protected HashEntry(HashEntry next, int hashCode, Object value) {
    		this.next = next;
    		this.hashCode = hashCode;
    		this.value = value;
    	}
    	
    	public int hashCode() {
    		return hashCode;
    	}

		public Object getKey() {
			return key;
		}
		
		public Object getValue() {
			return value;
		}
		
		protected int getKeyHash() {
			return System.identityHashCode(key);
		}
    	
		public Object setValue(Object value) {
			notImplemented("HashEntry.setValue");
			return null;
		}
    }
    
    /**
     * A wrapper around an IdentityHashMap that is intended to be returned by its
     * keySet() method. It simply creates a keyIterator of the appropriate type.
     * 
     * @author Pavel Avgustinov
     */
    class KeySet extends AbstractSet {
    	private final IdentityHashMap parent;
    	
    	protected KeySet(IdentityHashMap parent) {
    		this.parent = parent;
    	}

		public void clear() {
			parent.clear();
		}

		public Iterator iterator() {
			return parent.keyIterator();
		}

		public int size() {
			return parent.size();
		}
    }
    
    /**
     * An iterator over the key set of an IdentityHashMap. This class doesn't quite 
     * conform to the Iterator contract --- if keys can expire, it is impossible to
     * guarantee that after hasNext() terminates and before next() is called, the
     * key won't expire. Thus, if hasNext() here returns true, and next() returns
     * non-null, then there is a further key/value pair.
     * 
     * Clever handling of keys expiring while the iteration is in progress ensures the
     * most sensible possible behaviour.
     * 
     * @author Pavel Avgustinov
     */
    class KeyIterator implements Iterator {
        /** The parent map */
        private final IdentityHashMap parent;
        /** The current index into the array of buckets */
        protected int hashIndex;
        /** The last returned entry */
        protected HashEntry last = null;
        /** The next entry */
        protected HashEntry next = null;
        /** The modification count expected */
        protected int expectedModCount;
   	
        protected KeyIterator(IdentityHashMap parent) {
        	this.parent = parent;
        	this.expectedModCount = parent.modCount;
        	HashEntry[] data = parent.data;
        	int i = 0;
        	while(next == null && i < data.length) next = data[i++];
        	hashIndex = i;
        }
        
        public boolean hasNext() {
        	return (next != null);
        }

		public Object next() {
			if(parent.modCount != expectedModCount) {
				throw new ConcurrentModificationException("Unsafe modification of hashmap");
			}
			int i = hashIndex;
			HashEntry[] data = parent.data;
			do {
				if(next == null) return null;
				last = next;
				next = last.next;
				while(next == null && i < data.length) next = data[i++];
			} while(!last.live); 
			hashIndex = i;
			return last.getKey();
		}

		public void remove() {
			if(parent.modCount != expectedModCount) {
				throw new ConcurrentModificationException("Unsafe modification of hashmap");
			}
			if(last == null) throw new NoSuchElementException();
			if(!last.live) return;
			parent.remove(last.getKey());
			expectedModCount = parent.modCount;
		}
    }

    /**
     * A wrapper around an IdentityHashMap (or derived classes) that is intended
     * to be returned by entrySet() methods. It basically just returns an
     * iterator over the entries.
     * 
     * @author Pavel Avgustinov
     */
    class EntrySet extends AbstractSet {
    	private final IdentityHashMap parent;
    	
    	protected EntrySet(IdentityHashMap parent) {
    		this.parent = parent;
    	}
    	
    	public void clear() {
    		parent.clear();
    	}
    	
    	public Iterator iterator() {
    		return parent.entryIterator();
    	}
    	
    	public int size() {
    		return parent.size();
    	}
    }
    
    /**
     * An iterator over the entry set of an IdentityHashMap. This class doesn't quite 
     * conform to the Iterator contract --- if keys can expire, it is impossible to
     * guarantee that after hasNext() terminates and before next() is called, the
     * key won't expire. Thus, if hasNext() here returns true, and next() returns
     * non-null, then there is a further key/value pair.
     * 
     * Moreover, and more subtly, in the presence of concurrency it is possible for
     * key/value pairs to expire *after* a HashEntry has been returned by the iterator.
     * The only way to deal with this is to assign the result of getKey to a strong
     * reference and check it for nullness -- if it turns out to be null, ignore the
     * HashEntry and continue with the next() result.
     * 
     * Clever handling of keys expiring while the iteration is in progress ensures the
     * most sensible possible behaviour.
     * 
     * @author Pavel Avgustinov
     */
    class EntryIterator implements Iterator {
        /** The parent map */
        private final IdentityHashMap parent;
        /** The current index into the array of buckets */
        protected int hashIndex;
        /** The last returned entry */
        protected HashEntry last = null;
        /** The next entry */
        protected HashEntry next = null;
        /** The modification count expected */
        protected int expectedModCount;
   	
        protected EntryIterator(IdentityHashMap parent) {
        	this.parent = parent;
        	this.expectedModCount = parent.modCount;
        	HashEntry[] data = parent.data;
        	int i = 0;
        	while(next == null && i < data.length) next = data[i++];
        	hashIndex = i;
        }
        
        public boolean hasNext() {
        	return (next != null);
        }

		public Object next() {
			if(parent.modCount != expectedModCount) {
				throw new ConcurrentModificationException("Unsafe modification of hashmap");
			}
			int i = hashIndex;
			HashEntry[] data = parent.data;
			do {
				if(next == null) return null;
				last = next;
				next = last.next;
				while(next == null && i < data.length) next = data[i++];
			} while(!last.live); 
			hashIndex = i;
			return last;
		}

		public void remove() {
			if(parent.modCount != expectedModCount) {
				throw new ConcurrentModificationException("Unsafe modification of hashmap");
			}
			if(last == null) throw new NoSuchElementException();
			if(!last.live) return;
			parent.remove(last.getKey());
			expectedModCount = parent.modCount;
		}
    }

    /**
     * Return the number of key/value pairs in the map.
     */
	public int size() {
		return size;
	}
	
	
	public IdentityHashMap() {
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_THRESHOLD);
	}
	
	public IdentityHashMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}
	
	public IdentityHashMap(int initialCapacity, float loadFactor) {
		this(initialCapacity, loadFactor, calculateThreshold(initialCapacity, loadFactor));
	}
	
	public IdentityHashMap(int initialCapacity, float loadFactor, int threshold) {
		// Avoid normalising if we're just using the default capacity, which is already
		// a power of 2
		if(initialCapacity != DEFAULT_CAPACITY)
			initialCapacity = calculateNewCapacity(initialCapacity);
		data = new HashEntry[initialCapacity];
		this.loadFactor = loadFactor;
		this.threshold = threshold;
	}
 
	/**
     * Calculates the new capacity of the map.
     * This implementation normalizes the capacity to a power of two.
     * 
     * @param proposedCapacity  the proposed capacity
     * @return the normalized new capacity
     */
    protected static int calculateNewCapacity(int proposedCapacity) {
        int newCapacity = 1;
        if (proposedCapacity > MAXIMUM_CAPACITY) {
            newCapacity = MAXIMUM_CAPACITY;
        } else {
            while (newCapacity < proposedCapacity) {
                newCapacity <<= 1;  // multiply by two
            }
            if (newCapacity > MAXIMUM_CAPACITY) {
                newCapacity = MAXIMUM_CAPACITY;
            }
        }
        return newCapacity;
    }
    
    /**
     * Calculates the new threshold of the map, where it will be resized.
     * This implementation uses the load factor.
     * 
     * @param newCapacity  the new capacity
     * @param factor  the load factor
     * @return the new resize threshold
     */
    protected static int calculateThreshold(int newCapacity, float factor) {
        return (int) (newCapacity * factor);
    }
    
    /**
     * Calculates the hash bucket this hashCode should go into.
     * @param hashCode the pre-computed hashCode of the object
     * @return an index into the buckets array
     */
    protected int hashIndexFromCode(int hashCode) {
    	return hashCode & (data.length-1);
    }
    
    /**
     * Calculates the hash bucket this hashCode should go into, assuming the given
     * number of buckets
     * @param hashCode the pre-computed hashCode of the object
     * @param capacity the target number of buckets
     * @return an index into the buckets array
     */
    protected int hashIndexFromCode(int hashCode, int capacity) {
    	return hashCode & (capacity - 1);
    }
    
    /**
     * Calculates the hash bucket for this key.
     */
    protected int hashIndex(Object key) {
    	return System.identityHashCode(key) & (data.length-1);
    }

    /**
     * Calculates the hash bucket for the given key, assuming the given number
     * of buckets.
     */
    protected int hashIndex(Object key, int capacity) {
    	return System.identityHashCode(key) & (capacity - 1);
    }

	/**
	 * Strictly speaking, this shoudn't be used for checking emptiness, since the
	 * intention is for subclasses to provide weak-key maps, i.e. maps that drop
	 * a key-value pair as soon as the (weakref) key expires. Since GC runs happen
	 * beyond the control of the program, this might happen in between a call to
	 * isEmpty() and the use of the map on the assumption it's non-empty.
	 * 
	 * The preferred way is creating a value iterator and using its next() method.
	 * If that returns null, then there are no more elements.
	 * 
	 * It is, however, safe to assume that if this method returns true, then the
	 * map will indeed be empty until more elements are added.
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	public boolean containsKey(Object key) {
		notImplemented("containsKey");
		return false;
	}

	public boolean containsValue(Object value) {
		notImplemented("containsValue");
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(Object key) {
		HashEntry cur = data[hashIndex(key)];
		while(cur != null) {
			if(cur.getKey() == key) {
				return cur.value;
			}
			cur = cur.next;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object put(Object key, Object value) {
		modCount++;
		//System.out.print("+");
		int hash = System.identityHashCode(key);
		int index = hashIndexFromCode(hash);
		HashEntry cur = data[index];
		if(cur == null) {
			data[index] = createHashEntry(null, key, hash, value);
			size++;
		} else {
			while(cur != null) {
				if(cur.getKey() == key) {
					Object old = cur.value;
					cur.value = value;
					return old;
				}
				cur = cur.next;
			}
			data[index] = createHashEntry(data[index], key, hash, value);
			size++;
		}
		checkCapacity();
		return null;
	}

	HashEntry createHashEntry(HashEntry next, Object key, int hash, Object value) {
		return new HashEntry(next, key, hash, value);
	}
	
	/**
	 * Increases the number of buckets if necessary.
	 */
	protected void checkCapacity() {
		if(size >= threshold) {
			int n = data.length * 2;
			if(n <= MAXIMUM_CAPACITY) {
				rebalance(n);
			}
		}
	}
	
	/**
	 * Increases the number of buckets and re-distributes the entries into the new buckets.
	 * @param n the desired new number of buckets -- should be a power of 2.
	 */
	protected void rebalance(int n) {
		int old = data.length;
		if(n <= old) return;
		if(size == 0) {
			threshold = calculateThreshold(n, loadFactor);
			data = new HashEntry[n];
		} else {
			HashEntry[] oldEntries = data;
			HashEntry[] newEntries = new HashEntry[n];
			HashEntry cur, next;
			int index;
			modCount++;
			for(int i = 0; i < old; i++) {
				cur = oldEntries[i];
				if(cur != null) {
					oldEntries[i] = null;
					do {
						next = cur.next;
						index = hashIndexFromCode(cur.getKeyHash(), n);
						cur.next = newEntries[index];
						newEntries[index] = cur;
						cur = next;
					} while(cur != null);
				}
			}
			threshold = calculateThreshold(n, loadFactor);
			data = newEntries;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object remove(Object key) {
		Object result = safeRemove(key);
		if(result != null) modCount++;
		return result;
	}
	
	/**
	 * Removes the key/value pair corresponding to the hidden key in a "safe" way.
	 * 
	 * What this means is that it doesn't update the modCount. It's intended for
	 * this method to be called when automatically purging values from the map.
	 * In this way, we allow removal of values while iterating over the keyset,
	 * but still catch "non-safe", i.e. user-initiated, removals.
	 * 
	 * 
	 */
	protected Object safeRemove(Object key) {
		//System.out.print("-");
		int index = hashIndex(key);
		HashEntry cur = data[index];
		HashEntry prev = null;
		while(cur != null) {
			if(cur.getKey() == key) {
				Object old = cur.value;
				cur.live = false;
				size--;
				if(prev == null) {
					data[index] = cur.next;
				} else {
					prev.next = cur.next;
				}
				return old;
			}
			prev = cur;
			cur = cur.next;
		}
		return null;
	}

	public void putAll(Map arg0) {
		notImplemented("putAll");
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		modCount++;
		size = 0;
		HashEntry[] data = this.data;
		for(int i = 0; i < data.length; i++) {
			data[i] = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set keySet() {
		return new KeySet(this);
	}

	/**
	 * Constructs a KeyIterator object
	 */
	protected KeyIterator keyIterator() {
		return new KeyIterator(this);
	}
	
	/**
	 * Constructs an EntryIterator object
	 */
	protected EntryIterator entryIterator() {
		return new EntryIterator(this);
	}
	
	public Collection values() {
		notImplemented("values");
		return null;
	}

	public Set entrySet() {
		return new EntrySet(this);
	}

}
