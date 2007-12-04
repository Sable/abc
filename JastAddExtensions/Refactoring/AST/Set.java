
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;


  public class Set extends java.lang.Object {
    // Declared in Sets.jrag at line 8
    // map each element to an integer
    // perform lookup using a map and reverse lookup using an array
    private static HashMap elementMap = new HashMap();

    // Declared in Sets.jrag at line 9
    private static ArrayList elementList = new ArrayList();

    // Declared in Sets.jrag at line 10
    private static int index(Object element) {
      if(!elementMap.containsKey(element)) {
        elementMap.put(element, new Integer(elementList.size()));
        elementList.add(element);
      }
      return ((Integer)elementMap.get(element)).intValue();
    }

    // Declared in Sets.jrag at line 17
    private static Object element(int index) {
      return elementList.get(index);
    }

    // Declared in Sets.jrag at line 22

    // represent set using a bitfield
    int[] bits;

    // Declared in Sets.jrag at line 26

    // iterate over the set by searching the bitfield linearly for one bits
    // use the array to perform the reverse lookup
    public Iterator iterator() {
      return new Iterator() {
        private int nextElement;
        {
          // make nextElement refer to the first element
          nextElement = 0;
          hasNext();
        }
        public boolean hasNext() {
          while((nextElement >> 5) < bits.length) {
            int offset = nextElement >> 5;
            int bit = 1 << (nextElement & 0x1f);
            if((bits[offset] & bit) == bit)
              return true;
            nextElement++;
          }
          return false;
        }
        public Object next() {
          return element(nextElement++);
        }
        public void remove() {
          throw new Error("remove not supported for " + Set.this.getClass().getName());
        }
      };
    }

    // Declared in Sets.jrag at line 54

    // the empty set
    public static Set empty() {
      return emptySet;
    }

    // Declared in Sets.jrag at line 58
    // override set operations when operating on the empty set 
    private static Set emptySet = new Set(0) {
      public Set union(Set set) {
        return set;
      }
      public Set union(Object element) {
        int index = index(element);
        int offset = index >> 5;
        int bit = 1 << (index & 0x1f);
        Set set = new Set(offset + 1);
        set.bits[offset] = bit;
        return set;
      }
      public Set compl(Set set) {
        return this;
      }
      public Set compl(Object element) {
        return this;
      }
      public Set intersect(Set set) {
        return this;
      }
      public boolean isEmpty() {
        return true;
      }
    };

    // Declared in Sets.jrag at line 85
    
    // the full set
    public static Set full() {
      return fullSet;
    }

    // Declared in Sets.jrag at line 89
    // override set operations when operating on the full set 
    private static Set fullSet = new Set(0) {
      public Set union(Set set) {
        return this;
      }
      public Set union(Object element) {
        return this;
      }
      public Set compl(Set set) {
        throw new Error("comp not supported for the full set");
      }
      public Set compl(Object element) {
        throw new Error("comp not supported for the full set");
      }
      public Set intersect(Set set) {
        return set;
      }
      public boolean isEmpty() {
        return false;
      }
    };

    // Declared in Sets.jrag at line 111

    // create a new set containing at most size * 32 elements
    private Set(int size) {
      bits = new int[size];
    }

    // Declared in Sets.jrag at line 115
    // create a new set containing at most size * 32 elements and copy elements from set
    private Set(Set set, int size) {
      this(size);
      for(int i = 0; i < set.bits.length; i++)
        bits[i] = set.bits[i];
    }

    // Declared in Sets.jrag at line 121
    // create a copy of set
    private Set(Set set) {
      this(set, set.bits.length);
    }

    // Declared in Sets.jrag at line 125

    public Set union(Set set) {
      Set min, max;
      if(bits.length >= set.bits.length) {
        max = this;
        min = set;
      }
      else {
        max = set;
        min = this;
      }
      int length = min.bits.length;
      int i = 0;
      // search for elements in the smaller set that are missing in the larger set
      while(i < length && (max.bits[i] & min.bits[i]) == min.bits[i])
        i++;
      if(i != length) {
        // copy the larger set and store missing elements from smaller set
        max = new Set(max);
        for(; i < length; i++)
          max.bits[i] |= min.bits[i];
      }
      return max;
    }

    // Declared in Sets.jrag at line 148
    public Set union(Object element) {
      int index = index(element);
      int offset = index >> 5;
      int bit = 1 << (index & 0x1f);
      if(bits.length > offset && (bits[offset] & bit) == bit)
        return this;
      // copy the set and store the missing element
      Set set = new Set(this, Math.max(offset + 1, bits.length));
      set.bits[offset] |= bit;
      return set;
    }

    // Declared in Sets.jrag at line 159
    public void mutatingUnion(Object element) {
      int index = index(element);
      int offset = index >> 5;
      int bit = 1 << (index & 0x1f);
      if(bits.length > offset && (bits[offset] & bit) == bit)
        return;
      // Create new set
      int[] newBits = new int[Math.max(offset + 1, bits.length)];
      // Copy the to new bitset
      for(int i = 0; i < bits.length; i++)
        newBits[i] = bits[i];
      bits = newBits;
      // Add new bit
      bits[offset] |= bit;
    }

    // Declared in Sets.jrag at line 174
    public Set compl(Set set) {
      Set min, max;
      if(bits.length >= set.bits.length) {
        max = this;
        min = set;
      }
      else {
        max = set;
        min = this;
      }
      int length = min.bits.length;
      int i = 0;
      // search for elements in the smaller set that are available in the larget set
      while(i < length && (max.bits[i] & min.bits[i]) == 0)
        i++;
      if(i != length) {
        // copy the larger set and remove elements available in the smaller set
        max = new Set(max);
        for(; i < length; i++)
          max.bits[i] &= ~min.bits[i];
      }
      return max;
    }

    // Declared in Sets.jrag at line 197
    public Set compl(Object element) {
      int index = index(element);
      int offset = index >> 5;
      int bit = 1 << (index & 0x1f);
      if(bits.length > offset && (bits[offset] & bit) == 0)
        return this;
      // copy the set and remove element
      Set set = new Set(this, Math.max(offset + 1, bits.length));
      set.bits[offset] &= ~bit;
      return set;
    }

    // Declared in Sets.jrag at line 208
    public Set intersect(Set set) {
      if(this.equals(set))
        return this;
      if(set == fullSet) return this;
      int length = Math.max(this.bits.length, set.bits.length);
      Set newSet = new Set(length);
      for(int i = 0; i < length; i++)
        newSet.bits[i] = this.bits[i] & set.bits[i];
      return newSet;
    }

    // Declared in Sets.jrag at line 219

    public boolean contains(Object o) {
      int index = index(o);
      int offset = index >> 5;
      int bit = 1 << (index & 0x1f);
      return offset < bits.length && (bits[offset] & bit) != 0;
    }

    // Declared in Sets.jrag at line 226

    public boolean equals(Object o) {
      if(this == o) return true;
      if(o instanceof Set) {
        Set set = (Set)o;
        int length = set.bits.length > bits.length ? bits.length : set.bits.length;
        int i = 0;
        for(; i < length; i++)
          if(bits[i] != set.bits[i])
            return false;
        for(; i < bits.length; i++)
          if(bits[i] != 0)
            return false;
        for(; i < set.bits.length; i++)
          if(set.bits[i] != 0)
            return false;
        return true;
      }
      return super.equals(o);
    }

    // Declared in Sets.jrag at line 246

    public boolean isEmpty() {
      for(int i = 0; i < bits.length; i++)
        if(bits[i] != 0)
          return false;
      return true;
    }


}
