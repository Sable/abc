package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Create new CflowSetup instances.
 *  Handles CSE whenever possible to avoid
 *  creating instances for the same pointcut
 *  to eliminate redundant stacks/counters at
 *  runtime.
 *  @author damien
 */ 

public class GlobalCflowSetupFactory {

    /* Remembering which pointcuts we've seen (inside cflows),
       and the cflowsetup instances we created for each - must
       keep a set of these as one may match */

    private static class CfsStore {
       // Keep a list of pairs (pc, cfs): each pointcut pc already seen
       // maps to one or more CflowSetup objects. 
       
	private static class CflowEntry {
	    private Pointcut pc;
	    private CflowSetup s;

	    Pointcut getPc() { return pc; }
	    CflowSetup getCfs() { return s; }

	    public CflowEntry(Pointcut pc, CflowSetup s) {
		this.pc = pc; this.s = s;
	    }
	}

	private static LinkedList/*<CflowEntry>*/ cfsStore = new LinkedList();

	/* getIfExists(k): returns the elem that k maps to, or null if none 

	static HashSet<CflowSetup> getIfExists(Pointcut k) {
	    Iterator it = cfsStore.iterator();

	    while (it.hasNext()) {
		CflowEntry cfe = (CflowEntry)it.next();
		if (cfe.getPc().equivalent(k)) return cfe.getCfs();
	    }

	    return null;
	}
*/

	private static class AllMatchesIterator implements Iterator {
		private Pointcut pc;
		private Iterator it;
		private CflowSetupContainer cfsc;
		
		public AllMatchesIterator(Pointcut k) {
			this.pc = k;
			this.it = CfsStore.cfsStore.iterator();
		}
		
		private static Hashtable/*<Var,Var>*/ 
						inversemap(Hashtable/*<Var,Var>*/ renaming) {
			Hashtable/*<Var,Var>*/ newrenaming = new Hashtable();
			Enumeration enum = renaming.keys();
			while (enum.hasMoreElements()) {
				Var k = (Var)enum.nextElement();
				Var e = (Var)renaming.get(k);
				newrenaming.put(e, k);	
			}
			return newrenaming;
		}
		
		public boolean hasNext() {
			while (it.hasNext()) {
				CflowEntry cfe = (CflowEntry)it.next();
				Hashtable/*<String,Var>*/ renaming = new Hashtable();
				
				if (cfe.getPc().equivalent(pc, renaming)) {
					// Check that we can go the other way: 
					// quick fix for the problem of subclasses and equivalent():
					if (pc.equivalent(cfe.getPc(), inversemap(renaming))) {
						cfsc = new
							CflowSetupContainer(cfe.getCfs(), false, renaming);
						return true;						
					}
				}
			}
			return false;
		}
		
		public Object next() {
			return cfsc;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/* allMatches(k) : returns an iterator that goes through the matches 
	 * For each match return a CflowSetupContainer with the corresponding
	 * substitution in, and FALSE as isFresh() */

	static Iterator allMatches(Pointcut k) {
		return new AllMatchesIterator(k);
	}

	/* put(k, s): adds the mapping k |-> s to the map */

	static void put(Pointcut k, CflowSetup s) {
	    cfsStore.addFirst(new CflowEntry(k, s));
	}

	static void reset() {
	    cfsStore = new LinkedList();
	}

    }

    /*  Check whether two cflows can share the same stack/counter.
     *  Notes on sharing:
     *    ALL cflow stacks are static and are updated at each relevant
     *      join point - so don't need to check that in the same aspect,
     *      and singleton vs/percfow/perinstance makes no difference
     *    Must keep precedence so as not to break the order in which 
     *      cflow stacks are updated - this is determined by cf/cfbelow
     *      and the depth - make sure that both are identical
     *  CHECK THAT THESE CONDITIONS ARE SUFFICIENT
     *  ARE THEY NECESSARY?
     */

    private static boolean canShare(Aspect a,
				    boolean isBelow,
				    int depth,
				    CflowSetup cfs
				    ) {
	/* Aspect check - don't need since cflow stacks are static

	Aspect b = cfs.getAspect();

	boolean aspcheck = 
	    a.equals(b) || 
	    (    a.getPer() instanceof Singleton
		 && b.getPer() instanceof Singleton);
	*/

	boolean cflowcheck = 
	    isBelow == cfs.isBelow();

	boolean depthcheck = 
	    depth == cfs.getDepth();

	return (cflowcheck && depthcheck);

    }

    /* Add a new cflowsetup */

    private static void registerCfsInstance(Pointcut pc, CflowSetup cfs) {

		CfsStore.put(pc, cfs);

    }

    /** Construct a new instance of CflowSetup, or return an
     *  existing instance if possible.
     *  This must only be called AFTER inlining */

    public static CflowSetupContainer construct(Aspect aspect,
						Pointcut pc,
						boolean isBelow,
						Hashtable typeMap,
						Position pos,
						int depth) {

	/* is there an old one we can use? if the debug flag is not set... */	

	if (!abc.main.Debug.v().dontShareCflowStacks) {

	Iterator it = CfsStore.allMatches(pc);
		
	while (it.hasNext()) {
		CflowSetupContainer cfsc = (CflowSetupContainer) it.next();

	    if (canShare(aspect, isBelow, depth, cfsc.getCfs())) {
			// We can share the cflowsetup (but to use it may need to apply the
			// renaming)
			return cfsc;
	    }
	}

	}

	/* else we need to create a new one */

	CflowSetup ncfs = CflowSetup.construct(aspect,pc,isBelow,typeMap,pos,depth);
	registerCfsInstance(pc, ncfs);
	
		// Create a new CflowSetupContainer
		// The substitution should be the identity map on the free vars in the pc,
		// rather than just the empty map 
		
		Hashtable/*<Var,Var>*/ newrename = new Hashtable();
		
		Iterator fvs = ncfs.getActuals().iterator();
		while (fvs.hasNext()) {
			Var fv = (Var)fvs.next();
			newrename.put(fv, fv);
		}
		
	return new CflowSetupContainer(ncfs, true, newrename);
    }


    public static class CflowSetupContainer {
	private CflowSetup cfs;
	private boolean isFresh;
	private Hashtable/*<String, Var>*/ renaming;

	public CflowSetupContainer(CflowSetup cfs, boolean isFresh, 
							   Hashtable/*<String, Var>*/ renaming) {
	    this.cfs = cfs;
	    this.isFresh = isFresh;
	    this.renaming = renaming;
	}

	public CflowSetup getCfs() {
	    return cfs;
	}

	public boolean isFresh() {
	    return isFresh;
	}

	public Hashtable/*<String, Var>*/ getRenaming() {
		return renaming;
	}

    }

    public static void reset() {
	CfsStore.reset();
    }

}
