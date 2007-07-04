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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fixed universe of values of type E. This can be used to generate memory-efficient counters for values of this universe.
 * @author Eric Bodden
 */
public class FixedUniverse<E> {
    
    protected Map<E,Integer> elemToIndex;

    public FixedUniverse(Collection<E> universe) {
        elemToIndex = new HashMap<E, Integer>();
        int i = 0;
        for (E e : universe) {
            elemToIndex.put(e, i++);
        }
        elemToIndex = Collections.unmodifiableMap(elemToIndex);
    }
    
    /**
     * Returns a new map from values of the universe to int values, initialized to 0.
     */
    public Map<E,Integer> newIntMap() {
        return new FixedUniverseMap();
    }
    
    public class FixedUniverseMap implements Map<E,Integer>, Cloneable {

        protected int[] array;
        
        private FixedUniverseMap() {
            array = new int[elemToIndex.size()];
        }
        
        /** 
         * Not supported.
         * @throws UnsupportedOperationException always
         */
        public void clear() {
            throw new UnsupportedOperationException("Cannot clear, because we have a fixed universe.");
        }

        /** 
         * Returns <code>true</code> if the element is in the universe.
         */
        public boolean containsKey(Object key) {
            return elemToIndex.containsKey(key);
        }

        /** 
         * Returns <code>true</code> if any element in the universe is mapped to this value.
         */
        public boolean containsValue(Object value) {
            if(!(value instanceof Integer)) return false;
            int intVal = (Integer) value;
            for(int i=0; i<array.length; i++) {
                if(array[i]==intVal) {
                    return true;
                }
            }
            return false;
        }

        /** 
         * {@inheritDoc}
         */
        public Set<Map.Entry<E, Integer>> entrySet() {
            Set<Map.Entry<E, Integer>> set = new HashSet<Entry<E,Integer>>();
            for (final E key : elemToIndex.keySet()) {
                final int index = elemToIndex.get(key);
                set.add(new Entry<E, Integer> () {

                    public E getKey() {
                        return key;
                    }

                    public Integer getValue() {
                        return array[index];
                    }

                    public Integer setValue(Integer value) {
                        int oldValue = getValue();
                        array[index] = value;
                        return oldValue;
                    }
                    
                    public String toString() {
                        return getKey() + "=" + getValue();
                    }
                }
                );
            }            
            return set;
        }

        /** 
         * Returns <code>null</code> if the key is not in the universe or its associated
         * value otherwise.
         */
        public Integer get(Object key) {
            if(containsKey(key)) {
                return array[elemToIndex.get(key)];
            } else {
                return null;
            }
        }

        /** 
         * Returns <code>true</code> if and only if the universe is empty.
         */
        public boolean isEmpty() {
            return elemToIndex.isEmpty();
        }

        /** 
         * Returns an unmodifiable key set (the universe).
         */
        public Set<E> keySet() {
            return elemToIndex.keySet();
        }

        /** 
         * Associates the key with the value.
         * @return the old associated value
         * @throws IllegalArgumentException if key is not in the universe
         */
        public Integer put(E key, Integer value) {
            Integer oldValue = get(key);
            Integer index = elemToIndex.get(key);            
            if(index==null) {
                throw new IllegalArgumentException("Element is not in the universe!");
            }
            array[index] = value;
            return oldValue;
        }

        /** 
         * Calls {@link #put(Object, Integer)} repeatedly on all elements in the map.
         */
        public void putAll(Map<? extends E, ? extends Integer> map) {
            for (Entry<? extends E,? extends Integer> entry : map.entrySet()) {
                put(entry.getKey(),entry.getValue());
            }            
        }

        /** 
         * Not supported.
         * @throws UnsupportedOperationException always
         */
        public Integer remove(Object key) {
            throw new UnsupportedOperationException("Cannot remove, because we have a fixed universe.");
        }

        /** 
         * Returns the size of the universe.
         */
        public int size() {
            assert elemToIndex.size()==array.length;
            return elemToIndex.size();
        }

        /** 
         * Returns a list of all associated integer values. The list can contain duplicates. 
         */
        public Collection<Integer> values() {
            List<Integer> res = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++) {
                res.add(array[i]);
            }
            return Collections.unmodifiableList(res);
        }
        
        /** 
         * Clones this map (deep copy). It is still limited to the same fixed universe.
         */
        @Override
        protected FixedUniverseMap clone() throws CloneNotSupportedException {
            FixedUniverseMap clone = (FixedUniverseMap) super.clone();
            clone.array = new int[array.length];
            System.arraycopy(array, 0, clone.array, 0, array.length);
            return clone;
        }
        
        /** 
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return entrySet().toString();
        }

    }

}
