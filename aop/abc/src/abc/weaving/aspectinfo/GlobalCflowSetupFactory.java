/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 damien
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

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
 *  @author Damien Sereni
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

	private static class AllMatchesIterator implements Iterator {
		private Pointcut pc;
		private Iterator it;
		private CflowSetupContainer cfsc;
		
		public AllMatchesIterator(Pointcut k) {
			this.pc = k;
			this.it = CfsStore.cfsStore.iterator();
		}
		
		public boolean hasNext() {
			while (it.hasNext()) {
				CflowEntry cfe = (CflowEntry)it.next();
				Hashtable/*<Var, PointcutVarEntry>*/ renaming = new Hashtable();
				
				if (cfe.getPc().canRenameTo(pc, renaming)) {
					cfsc = new
						CflowSetupContainer(cfe.getPc(), cfe.getCfs(), false, renaming);
					return true;
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
			//System.out.println("******** Found pointcut to share ********");
			//System.out.println("** "+pc+" ===>");
			//System.out.println("** "+cfsc.getPointcut());
			//System.out.println("** with renaming:");
			//System.out.print("** ");
				//Enumeration rn = cfsc.getRenaming().keys();
				//while (rn.hasMoreElements()) {
				//	Var v = (Var)rn.nextElement();
				//	PointcutVarEntry pve = (PointcutVarEntry)cfsc.getRenaming().get(v);
				//	System.out.print(v+"->"+pve);
				//	if (rn.hasMoreElements()) System.out.print(", ");
				//}
				//System.out.println("");
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
		
		Hashtable/*<Var,PointcutVarEntry>*/ newrename = new Hashtable();
		
		Iterator fvs = ncfs.getActuals().iterator();
		while (fvs.hasNext()) {
			Var fv = (Var)fvs.next();
			newrename.put(fv, new PointcutVarEntry(fv));
		}
		
	return new CflowSetupContainer(pc, ncfs, true, newrename);
    }

	/** A wrapper class for an optional Var value (used in Pointcut.canRenameTo)
	 */
	public static class PointcutVarEntry {

		private Var var;
		private boolean hasVar;
	
		public PointcutVarEntry() {
			this.hasVar = false;
		}
	
		public PointcutVarEntry(Var var) {
			this.var = var;
			this.hasVar = true;
		}
	
		public boolean hasVar() {
			return hasVar;
		}
		public Var getVar() {
			return var;
		}
	
		public boolean equals(Object o) {
			if (o.getClass() == this.getClass()) {
				if (!this.hasVar) return (!((PointcutVarEntry)o).hasVar());
				return var.equals(((PointcutVarEntry)o).getVar());
			} else return false;
		}

		public boolean equalsvar(Var v) {
			if (!hasVar) return false;
			return (var.equals(v));
		}

		public String toString() {
			if (hasVar) return var.toString();
			else return "X";
		}

	}

    public static class CflowSetupContainer {
    private Pointcut pc;	// The original pc associated with this cfs (DEBUG)
	private CflowSetup cfs;
	private boolean isFresh;
	private Hashtable/*<Var, PointcutVarEntry>*/ renaming;

	public CflowSetupContainer(Pointcut pc, CflowSetup cfs, boolean isFresh, 
							   Hashtable/*<Var, PointcutVarEntry>*/ renaming) {
		this.pc = pc;
	    this.cfs = cfs;
	    this.isFresh = isFresh;
	    this.renaming = renaming;
	}

	private Pointcut getPointcut() {
		return pc;
	}

	public CflowSetup getCfs() {
	    return cfs;
	}

	public boolean isFresh() {
	    return isFresh;
	}

	public Hashtable/*<Var, PointcutVarEntry>*/ getRenaming() {
		return renaming;
	}

    }

    public static void reset() {
	CfsStore.reset();
    }

}
