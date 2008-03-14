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

package abc.tmwpopt.fsanalysis.ds;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.PointsToSet;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.matching.SMNode;
import abc.tmwpopt.fsanalysis.mustalias.TMFlowAnalysis;

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
	
	
    protected HashMap<A,Shadow> posHistory;
    protected HashMap<A,Shadow> negHistory;

	/** The unique FALSE disjunct. It holds no mapping and no history. */
	public static Disjunct FALSE;

	protected HashMap<String,Set<A>> posVarBinding;
	protected HashMap<String,Set<A>> negVarBinding;

	protected TMFlowAnalysis flowAnalysis;
	
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
		this.posHistory = new HashMap<A, Shadow>();
		this.negHistory = new HashMap<A, Shadow>();
	}
	
	/**
	 * Adds bindings to this disjunct, adding the shadowId to the history of the new disjunct.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param bindings the bindings of that edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadow the shadow that triggered this edge
	 * @param from origin state
	 * @return the updated disjunct; this is a fresh instance, 
	 * the disjuncts of this copy hold the history of the disjuncts of this constraint plus
	 * the shadowId that is passed in
	 */
	public abstract Disjunct addBindingsForSymbol(Collection allVariables, Map<String,A> bindings, Shadow shadow, SMNode from);
		
	
	/**
	 * Adds negative bindings for the case where the given symbol is read by taking a <i>skip</i> edge in the program graph.
	 * Effectively this deletes all bindings which adhere to the binding which is passed in.
	 * Note that unlike in {@link #addBindingsForSymbol(Collection, Map, int, boolean)}
	 * here we do not need to update the history of the disjuncts, because we know that no skip-loop
	 * can ever possibly lead to a final node.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param bindings the bindings of that skip-edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadow the shadow of the shadow that triggered this edge
	 * @param configuration 
	 * @param analysis 
	 * @return the updated constraint; this is a fresh instance or {@link #FALSE} 
	 */
	public abstract Set addNegativeBindingsForSymbol(Collection allVariables, Map<String,A> bindings, Shadow shadow, Configuration configuration);
	
	/**
	 * Currently this just returns a clone of <code>this</code>. We need a must-alias and must--flow analysis
	 * in order to do anything more clever.
	 * @param varName the name of the variable for which the binding is to be updated
	 * @param negBinding the negative binding this variable should be updated with
	 * @param shadow the shadow-id of the shadow that triggered this edge
	 * @return
	 */
	protected abstract Disjunct addNegativeBindingsForVariable(String varName, A negBinding, Shadow shadow);
	
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
			//clone history
			clone.posHistory = (HashMap<A, Shadow>) posHistory.clone();
			clone.negHistory = (HashMap<A, Shadow>) negHistory.clone();
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
		//symmetric over both histories
		int historyCode = ((posHistory == null) ? 0 : posHistory.hashCode()) + ((negHistory == null) ? 0 : negHistory.hashCode());
		result = prime * result + historyCode;
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
		if (!getCurrentHistory().equals(other.getCurrentHistory()))
			return false;
		return true;
	}

	public Collection<Shadow> getCurrentHistory() {
		Set<Shadow> history = new HashSet<Shadow>(posHistory.values());
		history.addAll(negHistory.values());
		return history;
	}
	
	public Disjunct<A> cloneWithoutHistory() {
		Disjunct<A> clone = clone();
		clone.posHistory.clear();
		clone.negHistory.clear();
		return clone;
	}

	public void setFlowAnalysis(TMFlowAnalysis flowAnalysis) {
		assert this.flowAnalysis == null;
		this.flowAnalysis = flowAnalysis;
	}
	
	public void removeFromPosShadowHistory(A binding) {
		Shadow removed = posHistory.remove(binding);
		assert removed!=null;
	}
	
	public void removeFromNegShadowHistory(A binding) {
		Shadow removed = negHistory.remove(binding);
		assert removed!=null;
	}
	
	public void reconcileHistory() {
		Set<A> allAs = new HashSet<A>();
		for (Set<A> as : posVarBinding.values()) {
			allAs.addAll(as);
		}
		for (Iterator<Map.Entry<A,Shadow>> entryIter = posHistory.entrySet().iterator(); entryIter.hasNext();) {
			Entry<A, Shadow> entry = entryIter.next();
			if(!allAs.contains(entry.getKey())) {
				entryIter.remove();
			}
		}
		allAs.clear();
		for (Set<A> as : negVarBinding.values()) {
			allAs.addAll(as);
		}
		for (Iterator<Map.Entry<A,Shadow>> entryIter = negHistory.entrySet().iterator(); entryIter.hasNext();) {
			Entry<A, Shadow> entry = entryIter.next();
			if(!allAs.contains(entry.getKey())) {
				entryIter.remove();
			}
		}
	}
	
	public static void reset() {
		FALSE = null;
	}
}