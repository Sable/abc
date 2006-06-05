/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
 * Copyright (C) 2006 Eric Bodden
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.types.SemanticException;
import polyglot.util.Position;
import soot.SootClass;
import soot.SootMethod;
import abc.weaving.matching.LocalsDecl;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;

/** Declare local pointcut variables. These can appear
 *  after inlining
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Eric Bodden
 */
public class LocalPointcutVars extends Pointcut {
    private Pointcut pc;
    private List/*<Formal>*/ formals;

    public LocalPointcutVars(Pointcut pc,List/*<Formal>*/ formals, Position pos) {
	super(pos);
	this.pc = pc;
	this.formals = formals;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public List getFormals() {
	return formals;
    }


    public Residue matchesAt(MatchingContext mc) 
	throws SemanticException
    {
    WeavingEnv we = mc.getWeavingEnv();
    SootClass cls = mc.getSootClass();
    SootMethod method = mc.getSootMethod();
    ShadowMatch sm = mc.getShadowMatch();
        
	WeavingEnv lwe=new LocalsDecl(formals,we);
	return pc.matchesAt(new MatchingContext(lwe,cls,method,sm));
    }

    public Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context,
			      int cflowdepth) {

	List/*<Formal>*/ newFormals=new ArrayList(formals.size());

	Hashtable newRenameEnv=new Hashtable(renameEnv);
	Hashtable newTypeEnv=new Hashtable(typeEnv);

	Iterator it=formals.iterator();
	while(it.hasNext()) {
	    Formal old=(Formal) it.next();

	    String newName=Pointcut.freshVar();
	    newFormals.add(new Formal(old.getType(),newName,old.getPosition()));

	    newRenameEnv.put(old.getName(),new Var(newName,old.getPosition()));
	    newTypeEnv.put(old.getName(),old.getType());
	}
	
	Pointcut pc=this.pc.inline(newRenameEnv,newTypeEnv,context,cflowdepth);
	if(pc==this.pc) return this;
	else return new LocalPointcutVars(pc,newFormals,getPosition());
    }

    public DNF dnf() {
	return DNF.declare(pc.dnf(),formals);
    }



    public String toString() {
	return "local"+formals+" ("+pc+")";
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	Hashtable newTypeMap=new Hashtable(typeMap);
	Iterator it=formals.iterator();
	while(it.hasNext()) {
	    Formal f=(Formal) it.next();
	    newTypeMap.put(f.getName(),f.getType());
	}
	pc.registerSetupAdvice(context,newTypeMap);
    }

    public void getFreeVars(Set/*<String>*/ result) {
    	pc.getFreeVars(result);
    	Iterator it=formals.iterator();
    	while(it.hasNext()) result.remove(((Formal) (it.next())).getName());
    }

    public boolean unify(Pointcut otherpc, Unification unification) {
		// Try to unify this.pc with otherpc
		
		// SPECIAL CASE: restricted unification
		// Then try to unify this.pc with otherpc, and check that the
		// local variables are not bound to anythin
		if (unification.unifyWithFirst()) {
			// Set the typemap
			addFormals(this.formals, unification.getTypeMap1());
			if (this.pc.unify(otherpc, unification)) {
				Iterator it = formals.iterator();
				while (it.hasNext()) {
					Formal f = (Formal)it.next();
					if (f.isInRenamingAsSource(unification.getRen2()))
						{ removeFormals(this.formals, unification.getTypeMap1());
						  return false;} 
					else
						{ f.removeFromRenamingAsSource(unification.getRen2());
						  f.removeFromRenamingAsSource(unification.getRen1()); } 
				}
				unification.setPointcut(this);
				// Clear the formals
				removeFormals(this.formals, unification.getTypeMap1());
				return true;
			} else { removeFormals(this.formals, unification.getTypeMap1());
					 return false;} 
		}
		
		// ELSE we can do straightforward unification
		else
		return unifyLocalsGen(this, otherpc, this, this.pc, otherpc, 1, unification);
		
	}
	
	public static boolean unifyLocals(Pointcut sourcepc, Pointcut destpc,
										Unification unification) {
		if (destpc instanceof LocalPointcutVars) {
			// We are trying to unify sourcepc with a LocalPointcutVars
			LocalPointcutVars destlocal = (LocalPointcutVars)destpc;
			
			// SPECIAL CASE: restricted unification
			// Try to unify sourcepc with destlocal.pc
			// and unset the bindings for any formals
			if (unification.unifyWithFirst()) {
				// Set the typemap
				addFormals(destlocal.getFormals(), unification.getTypeMap2());
				
				if (sourcepc.unify(destlocal.getPointcut(), unification)) {
					Iterator it = destlocal.getFormals().iterator();
					while (it.hasNext()) {
						Formal f = (Formal)it.next();
						unification.removeTargetAsString2(f.getName());
					}
					unification.setPointcut(sourcepc);
					
					// Clear the typemap and return
					removeFormals(destlocal.getFormals(), unification.getTypeMap2());
					return true;
				} else { removeFormals(destlocal.getFormals(), unification.getTypeMap2());
						 return false;} 
			}
			
			// ELSE we can do straightforward unification

			else			
			return unifyLocalsGen(sourcepc,destpc,destlocal,sourcepc,destlocal.getPointcut(), 2, unification);

		} else return false;
	}
	
	// A Generalised unifyLocals method, used by both unify and unifyLocals
	private static boolean unifyLocalsGen(Pointcut sourcepc, Pointcut destpc,
		LocalPointcutVars localspc, Pointcut sourceinner, Pointcut destinner,
		int dir /*1 if the local is source, 2 o/w*/,
		Unification unification) {
			
			// Add the formals to the typemap
			addFormals(localspc.getFormals(), unification.getTypeMap(dir));

			if (sourceinner.unify(destinner, unification)) {
				
				// Remove bindings into the locals pc that are abstracted away
				// by the Locals
				Iterator it = localspc.getFormals().iterator();
				while (it.hasNext()) {
					Formal f = (Formal)it.next();
					unification.removeTargetAsString(dir, f.getName());
				}
				
				// Find variables that are in the unified pc but not mapped to
				// a free var in either pointcut (anymore)
				
				List /*<Formal>*/ toRemove = new LinkedList();
				Enumeration keys = unification.keys1(); // The keys had better be the same for both rens
				while (keys.hasMoreElements()) {
					Var v = (Var)keys.nextElement();
					
					// Is v unbound?
					if ((!unification.isTargetSet1(v)) && (!unification.isTargetSet2(v))) {
						// Only way this could happen is if v was a formal to localspc
						Formal f = findFormalByName(localspc.getFormals(), v.getName());
						if (f == null)
							// SANITY CHECK: this shouldn't have happened
							throw new RuntimeException("Unification error: variable "+v+
							"is mapped to X under both renamings, but is not one of the" +							"LocalPointcutVars formals");
						
						// Mark f for abstraction
						toRemove.add(f);
					}
					
				}
				
				// If there are variables to abstract away, create a new Locals() pointcut
				if (!toRemove.isEmpty()) {
					LocalPointcutVars newpc = new
						LocalPointcutVars(unification.getPointcut(), toRemove, localspc.getPosition());
					unification.setPointcut(newpc);
				}
				// otherwise, the unification result is already set to the right thing 
 				
 				// Clean up the typemap, then return
 				removeFormals(localspc.getFormals(), unification.getTypeMap(dir));
 				return true;
			}
			else {
				// Clean up typemap
				removeFormals(localspc.getFormals(), unification.getTypeMap(dir));
				return false;
			}
			
			
		}
	
	private static void addFormals(List/*<Formal>*/ formals, Hashtable/*<String,AbcType>*/ typeMap) {
		Iterator it = formals.iterator();
		if (abc.main.Debug.v().debugPointcutUnification)
			System.out.println("Adding pointcut formals to typemap:");
		while (it.hasNext()) {
			Formal f = (Formal)it.next();
			// Sanity Checking
			if (typeMap.containsKey(f.getName()))
				throw new RuntimeException("ERROR: TypeMap already contains formal "+f.getName());
			typeMap.put(f.getName(), f.getType());
			if (abc.main.Debug.v().debugPointcutUnification) {
				System.out.print(f.getName() + " ");
			}
		}
		if (abc.main.Debug.v().debugPointcutUnification) System.out.println();
	}
	
	private static void removeFormals(List/*<Formal>*/ formals, Hashtable/*<String,AbcType>*/ typeMap) {
		Iterator it = formals.iterator();
		while (it.hasNext()) {
			Formal f = (Formal)it.next();
			typeMap.remove(f.getName());
		}
	}
	
	private static Formal findFormalByName(List/*<Formal>*/ formals, String name) {
		Iterator it = formals.iterator();
		while (it.hasNext()) {
			Formal f = (Formal) it.next();
			if (f.getName().equals(name))
				return f;
		}
		return null;
	}
	
}
