package test;

import java.util.ArrayList;
import java.util.List;

/**
 * A memo line is a list of memo pairs. Each memoised attribute has a memo line
 * recording the attributes it depends on (as closures), and their expected values.
 * 
 * In the general case, there would be multiple memo lines for different values of
 * an attribute's arguments, but in this example our attributes do not have arguments.
 */
public class MemoLine {
	
	protected final List<MemoPair> pairs = new ArrayList<MemoPair>();
	
	public boolean hit() {
		for(MemoPair p : pairs)
			if(!p.hit())
				return false;
		return true;
	}
	
	public <T> void add(Dependency dep, T val) {
		pairs.add(new MemoPair(dep, val));
	}
	
	public String toString() {
		return pairs.toString();
	}

  public void clear() {
    pairs.clear();
  }

}
