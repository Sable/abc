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
	// Our own implementation of (the relevant operations of) Map - 
	// Need to compare elements by equivalent, not equals, and no
	// guarantee that the hash codes are correct w.r.t. equiv
	// So keep it as an association list for the time being

	private static class CflowEntry {
	    private Pointcut pc;
	    private HashSet/*<CflowSetup>*/ s;

	    Pointcut getPc() { return pc; }
	    HashSet/*<CflowSetup>*/ getCfs() { return s; }

	    public CflowEntry(Pointcut pc, HashSet/*<CflowSetup>*/ s) {
		this.pc = pc; this.s = s;
	    }
	}

	private static LinkedList/*<CflowEntry>*/ cfsStore = new LinkedList();

	/* getIfExists(k): returns the elem that k maps to, or null if none */

	static HashSet/*<CflowSetup>*/ getIfExists(Pointcut k) {
	    Iterator it = cfsStore.iterator();

	    while (it.hasNext()) {
		CflowEntry cfe = (CflowEntry)it.next();
		if (cfe.getPc().equivalent(k)) return cfe.getCfs();
	    }

	    return null;
	}

	/* put(k, s): adds the mapping k |-> s to the map */

	static void put(Pointcut k, HashSet/*<CflowSetup>*/ s) {
	    cfsStore.addFirst(new CflowEntry(k, s));
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

    /* Get all cflowsetups stored for a particular pointcut */

    private static HashSet/*<CflowSetup>*/ getCfsInstances(Pointcut pc) {

	HashSet /*<CflowSetup>*/ cfss;

	cfss = CfsStore.getIfExists(pc);

	if (cfss == null) {
	    cfss = new HashSet();
	    CfsStore.put(pc, cfss);
	}

	return cfss;

    }

    /* Add a new cflowsetup */

    private static void registerCfsInstance(HashSet/*<CflowSetup>*/ cfsSet,
					    Pointcut pc, CflowSetup cfs) {

	// We know that cfs is the value that pc maps to under CflowStore
	// as cfs was the return value of getCfsInstances(pc)

	cfsSet.add(cfs);	

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

	/* is there an old one we can use? */	

	HashSet/*<CflowSetup>*/ cfsSet = getCfsInstances(pc);

	if (!abc.main.Debug.v().dontShareCflowStacks) {

	Iterator it = cfsSet.iterator();
	while (it.hasNext()) {
	    CflowSetup cfs = (CflowSetup) it.next();
	    if (canShare(aspect, isBelow, depth, cfs))		
		return new CflowSetupContainer(cfs, false);
	}

	}

	/* else we need to create a new one */

	CflowSetup ncfs = CflowSetup.construct(aspect,pc,isBelow,typeMap,pos,depth);
	registerCfsInstance(cfsSet, pc, ncfs);
	return new CflowSetupContainer(ncfs, true);
    }


    public static class CflowSetupContainer {
	private CflowSetup cfs;
	private boolean isFresh;

	public CflowSetupContainer(CflowSetup cfs, boolean isFresh) {
	    this.cfs = cfs;
	    this.isFresh = isFresh;
	}

	public CflowSetup getCfs() {
	    return cfs;
	}

	public boolean isFresh() {
	    return isFresh;
	}

    }

}
