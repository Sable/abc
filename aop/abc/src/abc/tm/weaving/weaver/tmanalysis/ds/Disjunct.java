/* abc - The AspectBench Compiler
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

package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import abc.tm.weaving.matching.SMNode;

/**
 * A disjunct represents a mapping of variables (type {@link String}) to
 * creation sites (type {@link PointsToSet}).
 * Also it holds a history, which is the set of shadow-ids of the shadows
 * of all edges in the program-graph that drove this disjunct into its current state.
 *
 * @param <A> the abstraction type representing an object
 * 
 * Disjuncts are produced using the prototype pattern, i.e. via cloning. The prototype is
 * {@link #FALSE}. Other Disjuncts can then be created by calling
 * {@link #addBindingsForSymbol(Collection, Map, String, boolean)} and  
 * {@link #addNegativeBindingsForSymbol(Collection, Map, String)}.
 *
 * @author Eric Bodden
 */
public abstract class Disjunct<A> implements Cloneable {
	
	
    protected HashMap<A,String> history;

	/**
	 * a most-recently used cache to cache equal disjuncts; the idea is that equality checks
	 * are faster if performed on "interned" instances
	 * @see #intern()
	 * @see String#intern()
	 */
	protected static Map disjunctToUniqueDisjunct = new HashMap();//new MemoryStableMRUCache("disjunct-intern",10*1024*1024,false);
	
	public static void reset() {
		disjunctToUniqueDisjunct.clear();
	}
	
	/** The unique FALSE disjunct. It holds no mapping and no history. */
	public static Disjunct FALSE;

	protected HashMap<String,Set<A>> posVarBinding;
	protected HashMap<String,Set<A>> negVarBinding;
	
	/**
	 * Creates a new disjunct with empty bindings and history.
	 * Only to be called form this class (prototype pattern.) 
	 * Other disjuncts can be created using
	 * {@link #addBindingsForSymbol(Collection, Map, String, boolean)} and 
	 * {@link #addNegativeBindingsForSymbol(Collection, Map, String)}.
	 */
	protected Disjunct() {
		this.posVarBinding = new HashMap<String, Set<A>>();
		this.negVarBinding = new HashMap<String, Set<A>>();
	}
	
	/**
	 * Adds bindings to this disjunct, adding the shadowId to the history of the new disjunct.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param bindings the bindings of that edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @param from origin state
	 * @return the updated disjunct; this is a fresh instance, 
	 * the disjuncts of this copy hold the history of the disjuncts of this constraint plus
	 * the shadowId that is passed in
	 */
	public abstract Disjunct addBindingsForSymbol(Collection allVariables, Map<String,A> bindings, String shadowId, SMNode from);
		
	
	/**
	 * Adds negative bindings for the case where the given symbol is read by taking a <i>skip</i> edge in the program graph.
	 * Effectively this deletes all bindings which adhere to the binding which is passed in.
	 * Note that unlike in {@link #addBindingsForSymbol(Collection, Map, int, boolean)}
	 * here we do not need to update the history of the disjuncts, because we know that no skip-loop
	 * can ever possibly lead to a final node.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param bindings the bindings of that skip-edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @param configuration 
	 * @param analysis 
	 * @return the updated constraint; this is a fresh instance or {@link #FALSE} 
	 */
	public abstract Set addNegativeBindingsForSymbol(Collection allVariables, Map<String,A> bindings, String shadowId, Configuration configuration);
	
	/**
	 * Currently this just returns a clone of <code>this</code>. We need a must-alias and must--flow analysis
	 * in order to do anything more clever.
	 * @param varName the name of the variable for which the binding is to be updated
	 * @param negBinding the negative binding this variable should be updated with
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @return
	 */
	protected abstract Disjunct addNegativeBindingsForVariable(String varName, A negBinding, String shadowId);
	
	/**
	 * {@inheritDoc}
	 */
	protected Disjunct<A> clone() {		
		try {
			Disjunct<A> clone = (Disjunct<A>) super.clone();
			//deep clone positive bindings
			clone.posVarBinding = (HashMap) posVarBinding.clone();
			for (Map.Entry<String,Set<A>> entry : clone.posVarBinding.entrySet()) {
				HashSet<A> clonedSet = (HashSet<A>) ((HashSet)entry.getValue()).clone();
				entry.setValue(clonedSet);
			}
			//deep clone negative bindings
			clone.negVarBinding = (HashMap) negVarBinding.clone();
			for (Map.Entry<String,Set<A>> entry : clone.negVarBinding.entrySet()) {
				HashSet<A> clonedSet = (HashSet<A>) ((HashSet)entry.getValue()).clone();
				entry.setValue(clonedSet);
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((negVarBinding == null) ? 0 : negVarBinding.hashCode());
		result = prime * result
				+ ((posVarBinding == null) ? 0 : posVarBinding.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		final Disjunct other = (Disjunct) obj;
		if (negVarBinding == null) {
			if (other.negVarBinding != null)
				return false;
		} else if (!negVarBinding.equals(other.negVarBinding))
			return false;
		if (posVarBinding == null) {
			if (other.posVarBinding != null)
				return false;
		} else if (!posVarBinding.equals(other.posVarBinding))
			return false;
		return true;
	}

	public Collection<String> getCurrentHistory() {
		return history.values();
	}
	
	public Disjunct<A> cloneWithoutHistory() {
		Disjunct<A> clone = clone();
		clone.history.clear();
		return clone;
	}
	
}