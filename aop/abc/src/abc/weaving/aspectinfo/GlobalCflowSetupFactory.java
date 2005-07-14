/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Damien Sereni
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

package abc.weaving.aspectinfo;

/**
 * @author Damien Sereni
 */

import java.util.*;
import abc.main.*;
import polyglot.util.Position;

public class GlobalCflowSetupFactory {

	private static void debug(String s) {
		if (Debug.v().debugCflowSharing) {
			System.out.println(s);
		}
	}
	
	private static void debugMapping(Hashtable/*<Var,PointcutVarEntry>*/ renaming) {
		if (Debug.v().debugCflowSharing) {
		Enumeration e = renaming.keys();
		while (e.hasMoreElements()) {
			Var v = (Var)e.nextElement();
			VarBox ve = 
				(VarBox)renaming.get(v);
			System.out.print(v+"->"+ve);
			if (e.hasMoreElements()) 
				System.out.print(", ");
		}
		System.out.println();
		}
	}
	
	/** Store the instances of CflowSetup that we've already
	 *  seen, together with the pointcut that they represent
	 */
	private static class CfsStore {
		private static class CfsEntry {
			private Pointcut pc;
			private CflowSetup cfs;
			private Hashtable/*<String,AbcType>*/ typeMap;
			
			Pointcut getPointcut() { return pc; }
			CflowSetup getCfs() { return cfs; }
			Hashtable/*<String,AbcType>*/ getTypeMap() { return typeMap; }
			
			CfsEntry(Pointcut pc, Hashtable/*<String,AbcTy?>*/ typeMap, CflowSetup cfs) {
				this.pc = pc; this.cfs = cfs; this.typeMap = typeMap;
			}
		}

	    public static void reset() {
		cfsStore.clear();
	    }
	
		private static List/*<CfsEntry>*/ cfsStore = new ArrayList();
		
		/** Add a new CflowSetup, if we could not share it
		 * 
		 * @param pc  The (textual) pointcut to associate with
		 * the cfs
		 * @param cfs The new CflowSetup
		 */
		static void put(Pointcut pc, Hashtable/*<String, AbcType>*/ typeMap, CflowSetup cfs) {
			cfsStore.add(new CfsEntry(pc, typeMap, cfs));
		}
	
		/** Try to find an existing CflowSetup that can be reused for this pointcut
		 * 
		 * @param pc The pointcut to find a Cfs for
		 * @param isBelow Is pc in a cflowbelow?
		 * @param depth The cflow depth that pc is located at
		 * @param unification returns the unification if it exists
		 * @return The index at which the Cfs can be found, -1 if none
		 */
		private static int find(Pointcut pc, boolean isBelow, int depth, Unification unification) {
			int index = 0;
			while (index < cfsStore.size()) {
				CfsEntry cfe = (CfsEntry)cfsStore.get(index);
				if (canShare(isBelow, depth, cfe.getCfs())) {
					// Reset the bindings from previous attempts
					unification.resetBindings();
					// Set unification.typeMap1 to be the typemap for this cfs
					unification.setTypeMap1(cfe.getTypeMap());
					
					if (cfe.getPointcut().unify(pc, unification)) {
						return index; 
					}
				}
				index++;
			}
			return -1;
		}
		
		private static CfsContainer findAndUnify(Aspect a, 
									  Pointcut pc, 
									  boolean isBelow, 
									  int depth, 
									  Position pos,
									  Hashtable typeMap,
									  Unification unification) {
			int index = find(pc, isBelow, depth, unification);
			if (index == -1) return null;
			
			CfsEntry cfe = (CfsEntry)cfsStore.get(index);
			
			if (unification.getPointcut() == cfe.getPointcut()) {
				// The unified pointcut is the existing one; we can keep the old cfs
				debug("*** Sharing: \n"+
					  pc+
					  "\n (depth "+depth+","+(isBelow?"cflowbelow":"cflow")+"). Keeping CFS for: \n"+
					  cfe.getPointcut()+
					  "\n (depth "+cfe.getCfs().getDepth()+","+(isBelow?"cflowbelow":"cflow")+") with mapping: ");
				debugMapping(unification.getRen2());
				return new CfsContainer(cfe.getCfs(),unification.getRen2());
			} else {
				// We need to create a new CFS, delete the old mapping
				// and reregister the new CFS with all the advice that 
				// used the old CFS
				debug("******** Unifying pointcuts (depth "+depth+"), "+(isBelow?"cflowbelow":"cflow"));
				debug("******** Old CFS pointcut:\n"+cfe.getPointcut());
				debug("******** Unified with:\n"+pc);
				debug("******** ...New pointcut:\n"+unification.getPointcut());
				Hashtable/*<String,AbcType>*/newTypeMap = new Hashtable();
				CflowSetup newcfs = 
					createNewUnificationCfs(a,isBelow,typeMap,newTypeMap,cfe.getCfs(),pos,depth,unification);
				cfsStore.remove(index);
				CfsEntry newcfe = new CfsEntry(unification.getPointcut(), newTypeMap, newcfs);
				cfsStore.add(newcfe);
				
				Iterator cflows = cfe.getCfs().getUses().iterator();
				while (cflows.hasNext()) {
					CflowPointcut p = (CflowPointcut)cflows.next();
					
					debug(">>>> Updating Cflow:\n"+p);
					
					Unification rename = new Unification(true);
					rename.setTypeMap1(newcfe.getTypeMap());
					rename.setTypeMap2(p.getTypeMap());
					debug("TypeMap for new CFS:\n"+newcfe.getTypeMap());
					debug("TypeMap for updated cflow:\n"+p.getTypeMap());
					
//					if (!unification.getPointcut().canRenameTo(p.getPointcut(), renaming)) 
					if (!unification.getPointcut().unify(p.getPointcut(), rename)) {
						throw new RuntimeException("Could not rename:\n "+unification.getPointcut()+
								"\n to \n"+p.getPointcut()+"\n when trying to replace a CFS");
					}
					debug("******** Unify with\n"+p.getPointcut()+"\nwith renaming:");
					debugMapping(rename.getRen2());
					p.reRegisterSetupAdvice(newcfs, rename.getRen2());
				}
				debug("******************************************");
				
				// Clear the uses for the old cfs
				cfe.getCfs().clearUses();
			
				return new CfsContainer(newcfe.getCfs(),unification.getRen2());
			}
		}
		
		private static CflowSetup createNewUnificationCfs(
				Aspect a, boolean isBelow, 
				Hashtable origTypeMap /*The typemap of the NEW pc*/,
				Hashtable newTypeMap /*To reveive the new type map*/,
				CflowSetup oldcfs /*The existing instance of Cfs*/,
				Position pos, int depth,
				Unification unification) {
			
			// Creating a new typemap for the renamed vars
			Set/*<String>*/ fvs = new HashSet();
			unification.getPointcut().getFreeVars(fvs);
			Iterator it = fvs.iterator();
			
			while (it.hasNext()) {
				String fv = (String)it.next();
				// Need to find the type of fv
				// It is mapped to a var in the new pc, to a var in the old (cfs) pc
				// or to both
				VarBox ve1 = 
					unification.getFromString1(fv);
				VarBox ve2 = 
					unification.getFromString2(fv);
				
				if (ve1.hasVar()) {
					Formal f1 = findFormal(ve1.getVar().getName(), oldcfs.getFormals());
					if (ve2.hasVar()) {
						// fv is mapped to a var in BOTH
						AbcType tp2 = (AbcType)origTypeMap.get(ve2.getVar().getName());
						if (!tp2.equals(f1.getType())) 
							throw new RuntimeException("Error: types are not equal after unification:\n"+
									fv+"->"+f1.getName()+" : "+f1.getType()+"\n"+
									fv+"->"+ve2.getVar()+" : "+tp2);
						newTypeMap.put(fv, tp2);
					} else {
						// fv is mapped to a var in 1 only
						newTypeMap.put(fv, f1.getType());
					}
				} else if (ve2.hasVar()) {
					// fv is mapped to a var in 2 only
					AbcType tp2 = (AbcType)origTypeMap.get(ve2.getVar().getName());
					newTypeMap.put(fv, tp2);
				} else
					// Could not find it in either pc: this is a unification bug
					throw new RuntimeException("Could not find variable "+fv+
							" in either of the unified pointcuts:");
				
			}
			
			// Create the cfs
			CflowSetup newcfs = CflowSetup.construct
			  (a, unification.getPointcut(), isBelow, newTypeMap, pos, depth);
			abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().addAdviceDecl(newcfs);
			
			// Return the new cfs
			return newcfs;
			
		}
		
		static CfsContainer findExisting
			(Aspect a, Pointcut pc, boolean isBelow, Hashtable typeMap, Position pos, int depth) {
			
			// Create a new (unrestricted) unification
			Unification unification = new Unification(false);
			
			// Set unfication.typeMap2 to be the typeMap for pc
			unification.setTypeMap2(typeMap);
			
			// Try to find it
			return findAndUnify(a,pc,isBelow,depth,pos,typeMap,unification);
			
		}
		
	}

	private static Formal findFormal(String fv, List/*<Formal>*/ fs) {
		Iterator it = fs.iterator();
		while (it.hasNext()) {
			Formal newf = (Formal)it.next();
			if (newf.getName().equals(fv))
				return newf;
		}
		throw new RuntimeException("Could not find formal "+fv);
	}
	
	private static void makeTypeMap(List/*<Formal>*/ fs, Hashtable/*<String,AbcType>*/ outTypeMap) {
		Iterator it = fs.iterator();
		while (it.hasNext()) {
			Formal newf = (Formal)it.next();
			outTypeMap.put(newf.getName(), newf.getType());
		}
	}
	
    private static boolean canShare(boolean isBelow, int depth, CflowSetup cfs) {

    		boolean cflowcheck = 
    			isBelow == cfs.isBelow();

    		boolean depthcheck = 
    			depth == cfs.getDepth();

    		return (cflowcheck && depthcheck);

    }
	
    private static CfsContainer buildNewCfs(
    		Aspect a, Pointcut pc, boolean isBelow, Hashtable typeMap, Position pos, int depth) {
		
    	debug("Creating a new CFS for (depth "+depth+","+(isBelow?"cflowbelow":"cflow")+"):\n"+pc);
		CflowSetup cfs = CflowSetup.construct(a, pc, isBelow, typeMap, pos, depth);
		
		// Need to create the identity renaming on pc
		Hashtable/*<Var,PointcutVarEntry>*/ renaming = new Hashtable();
		Iterator it = cfs.getActuals().iterator();
		while (it.hasNext()) {
			Var v = (Var)it.next();
			VarBox ve = 
				new VarBox(v);
			renaming.put(v, ve);
		}
		
		// Need to register the new CFS as an abstract advice decl
		abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().addAdviceDecl(cfs);
		
		// Need to remember that we've created cfs
		// IF THE DEBUG FLAG IS NOT SET (o/w no point)
		if (abc.main.options.OptionsParser.v().cflow_use_sharing())
			CfsStore.put(pc, typeMap, cfs);
		
		// Now return it
		return new CfsContainer(cfs, renaming);
    }
    
    public static CfsContainer construct(
    		Aspect a, Pointcut pc, boolean isBelow, Hashtable typeMap, Position pos, int depth) {
    	
    	if (!abc.main.options.OptionsParser.v().cflow_use_sharing())
    		return buildNewCfs(a, pc, isBelow, typeMap, pos, depth);
    	
    	CfsContainer cfsc = CfsStore.findExisting(a, pc, isBelow, typeMap, pos, depth);
    	
    	if (cfsc == null) {
    		// We need to create a new one
    		return buildNewCfs(a, pc, isBelow, typeMap, pos, depth);
    		
    	} else {
    		return cfsc;
    	}
    	
    }
    
    static class CfsContainer {
    	private CflowSetup cfs = null;
    	private Hashtable/*<Var,PointcutVarEntry>*/ renaming = null;
    	
    	public CfsContainer(CflowSetup cfs, Hashtable/*<Var, PointcutVarEntry>*/ renaming) {
    		this.cfs = cfs; this.renaming = renaming;
    	}
    	
    	public CflowSetup getCfs() { return cfs; }
    	public Hashtable/*<Var,PointcutVarEntry>*/ getRenaming() { return renaming; }
    }

    public static void reset() {
	CfsStore.reset();
    }
    
}
