
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

public class SmallSet extends java.lang.Object {
    // Declared in Sets.jrag at line 242

    HashSet set = new HashSet();

    // Declared in Sets.jrag at line 243

    public Iterator iterator() { return set.iterator(); }

    // Declared in Sets.jrag at line 244

    public static SmallSet empty() { return emptySet; }

    // Declared in Sets.jrag at line 245

    private static SmallSet emptySet = new SmallSet() {
      public SmallSet union(SmallSet set) { return set; }
      public SmallSet union(Object element) { return new SmallSet().union(element); }
      public SmallSet compl(SmallSet set) { return this; }
      public SmallSet compl(Object element) { return this; }
      public SmallSet intersect(SmallSet set) { return this; }
      public boolean isEmpty() { return true; }
    };

    // Declared in Sets.jrag at line 253

    public static SmallSet full() { return fullSet; }

    // Declared in Sets.jrag at line 254

    private static SmallSet fullSet = new SmallSet() {
      public SmallSet union(SmallSet set) { return this; }
      public SmallSet union(Object element) { return this; }
      public SmallSet compl(SmallSet set) {
    	  throw new Error("compl not supported for the full set");
      }
      public SmallSet compl(Object element) {
        throw new Error("compl not supported for the full set");
      }
      public SmallSet intersect(SmallSet set) { return set; }
      public boolean isEmpty() { return false; }
    };

    // Declared in Sets.jrag at line 267


    protected SmallSet() {
    }

    // Declared in Sets.jrag at line 269

    public SmallSet union(SmallSet set) {
      if(set.isEmpty() || this.equals(set))
        return this;
      SmallSet newSet = new SmallSet();
      newSet.set.addAll(this.set);
      newSet.set.addAll(set.set);
      return newSet;
    }

    // Declared in Sets.jrag at line 278

    
    public SmallSet union(Object element) {
      if(contains(element))
        return this;
      SmallSet newSet = new SmallSet();
      newSet.set.addAll(this.set);
      newSet.set.add(element);
      return newSet;
    }

    // Declared in Sets.jrag at line 287

    
    public SmallSet compl(SmallSet set) {
      if(set.isEmpty())
        return this;
      SmallSet newSet = new SmallSet();
      newSet.set.addAll(this.set);
      newSet.set.removeAll(set.set);
      return newSet;
    }

    // Declared in Sets.jrag at line 296

    
    public SmallSet compl(Object element) {
      if(!set.contains(element))
        return this;
      SmallSet newSet = new SmallSet();
      newSet.set.addAll(this.set);
      newSet.set.remove(element);
      return newSet;
    }

    // Declared in Sets.jrag at line 305

    
    public SmallSet intersect(SmallSet set) {
      if(this.equals(set) || set == fullSet)
        return this;
      SmallSet newSet = new SmallSet();
      newSet.set.addAll(this.set);
      newSet.set.retainAll(set.set);
      return newSet;
    }

    // Declared in Sets.jrag at line 314


    public boolean contains(Object o) {
      return set.contains(o);
    }

    // Declared in Sets.jrag at line 318


    public boolean equals(Object o) {
      if (o == null) return false;
      if(this == o) return true;
      if(o instanceof SmallSet) {
        SmallSet set = (SmallSet)o;
        return this.set.equals(set.set);
      }
      return super.equals(o);
    }

    // Declared in Sets.jrag at line 328


    public boolean isEmpty() {
      return set.isEmpty();
    }

    // Declared in Sets.jrag at line 331

    public void add(SmallSet set) {
      this.set.addAll(set.set);
    }

    // Declared in Sets.jrag at line 334

    public void add(Object o) {
      this.set.add(o);
    }

    // Declared in Sets.jrag at line 337

    public static SmallSet mutable() {
      return new SmallSet();
    }


}
