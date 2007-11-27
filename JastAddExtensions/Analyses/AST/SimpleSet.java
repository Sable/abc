
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

  // A persistent Set
  public interface SimpleSet {
    // Declared in DataStructures.jrag at line 4

    int size();

    // Declared in DataStructures.jrag at line 5

    boolean isEmpty();

    // Declared in DataStructures.jrag at line 6

    SimpleSet add(Object o);

    // Declared in DataStructures.jrag at line 7

    Iterator iterator();

    // Declared in DataStructures.jrag at line 8

    boolean contains(Object o);

    // Declared in DataStructures.jrag at line 9

    SimpleSet emptySet = new SimpleSet() {
      public int size() { return 0; }
      public boolean isEmpty() { return true; }
      public SimpleSet add(Object o) {
        if(o instanceof SimpleSet)
          return (SimpleSet)o;
        return new SimpleSetImpl().add(o);
      }
      public boolean contains(Object o) { return false; }
      public Iterator iterator() { return Collections.EMPTY_LIST.iterator(); }
    };

    // Declared in DataStructures.jrag at line 20

    SimpleSet fullSet = new SimpleSet() {
      public int size() { throw new Error("Operation size not supported on the full set"); }
      public boolean isEmpty() { return false; }
      public SimpleSet add(Object o) { return this; }
      public boolean contains(Object o) { return true; }
      public Iterator iterator() { throw new Error("Operation iterator not support on the full set"); }
    };

    // Declared in DataStructures.jrag at line 27

    class SimpleSetImpl implements SimpleSet {
      private HashSet internalSet;
      public SimpleSetImpl() {
        internalSet = new HashSet(4);
      }
      private SimpleSetImpl(SimpleSetImpl set) {
        this.internalSet = new HashSet(set.internalSet);
      }
      public int size() {
        return internalSet.size();
      }
      public boolean isEmpty() {
        return internalSet.isEmpty();
      }
      public SimpleSet add(Object o) {
        if(internalSet.contains(o)) return this;
        SimpleSetImpl set = new SimpleSetImpl(this);
        set.internalSet.add(o);
        return set;
      }
      public Iterator iterator() {
        return internalSet.iterator();
      }
      public boolean contains(Object o) {
        return internalSet.contains(o);
      }
    }

}
