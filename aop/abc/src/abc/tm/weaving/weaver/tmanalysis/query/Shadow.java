/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.Local;
import soot.PointsToSet;
import soot.PrimType;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.pointer.FullObjectSet;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.util.Naming;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch;

/**
 * A Shadow represents a static point in the program where the state
 * machine may make a transition. Each Shadow belongs to a container
 * method and has a uniqueShadowId. Shadows also have a points-to
 * set for each tracematch variable, and track one local belonging 
 * to each points-to set specifically.
 *
 * @author Eric Bodden
 */
public class Shadow {
	
	/** A mapping from each variable to its points-to sets. */
	protected Map varToPointsToSet;		

	/** A mapping from each variable to the {@link Local} that represents it in Jimple. */
	protected Map varToSootLocal;		

	/** Backwards mapping of {@link #varToSootLocal} */
	protected Map sootLocalToVar;		

	/** if after importing points-to sets this shadow has an empty variable-mapping, this will be set to true
	 * (can be used for error detection) */
	protected boolean hasEmptyMapping;

	/** the cached unique shadow ID of this shadow*/
	protected final String uniqueShadowId;

	/** the containing method */
	protected final SootMethod container;
    
	/**
	 * Creates a new shadow.
	 * @param match the shadow match for a symbol-shadow of a tracematch 
	 * @param container the containing method
	 */
	public Shadow(SymbolShadowMatch match, SootMethod container) {
		this.hasEmptyMapping = false;
		this.container = container;
		this.uniqueShadowId = match.getUniqueShadowId();
		importVariableMapping(match.getTmFormalToAdviceLocal(), container);
	}
	
	/**
	 * Imports a variable mapping into this shadow.
	 * @param mapping a mapping from variable names to {@link Local}s
	 * @param container the containing method
	 */
	protected void importVariableMapping(Map<String,Local> mapping, SootMethod container)  {
		varToPointsToSet = new HashMap();
		varToSootLocal = new HashMap();
		sootLocalToVar = new HashMap();
		
		for (Entry<String,Local> entry : mapping.entrySet()) {

			Local l = entry.getValue();
			PointsToSet pts;
			if(l.getType() instanceof PrimType) {
				//if the type of the variable is a primitive type, then we assume it could "point to anything", i.e. could have any value
				pts = FullObjectSet.v();
			} else {
				//if l is null this probably means that the WeavingVar was never woven
				assert l!=null;
				PointsToSet paddlePts = (PointsToSet) Scene.v().getPointsToAnalysis().reachingObjects(l);
				if(paddlePts.isEmpty()) {
					hasEmptyMapping = true;
					System.err.println("WARNING: Empty points-to set for variable "+l+" in "+container);
					/*   POSSIBLE CAUSES FOR EMPTY VARIABLE MAPPINGS:
					 *   1.) a shadow is created for an invoke statement (or similar) of the form o.foo() where o has type NullType, i.e. is certainly null.
					 *   2.) if object-sensitivity is enabled:
					 *       if dynamic type checks for advice aplication can be ruled out statically; 
					 *        example: point cut is call(* Set.add(..)) && args(Collection)
					 *                 shadow is s.add("someString")
					 *                 in this case, paddle sees automatically that the instanceof check in the bytecode
					 *                 can never succeed; hence the statement is rendered unreachable and the points-to set becomes empty
					 */
				}							
				//pts = new PaddlePointsToSetCompatibilityWrapper(paddlePts);
				pts = paddlePts;
			}
			String varName = entry.getKey();
			varToPointsToSet.put(varName,pts);
			varToSootLocal.put(varName, l);
			assert !sootLocalToVar.containsKey(l);
			sootLocalToVar.put(l, varName);
		}
			
	}
		
	public Map getVariableMapping() {
		return Collections.unmodifiableMap(new HashMap(varToPointsToSet));
	}
	
	public Local getLocalForVarName(String varName) {
		assert varToSootLocal.containsKey(varName);
		return (Local) varToSootLocal.get(varName);
	}
	
	public String getVarNameForLocal(Local l) {
		return (String) sootLocalToVar.get(l);
	}

	
	public Set getBoundVariables() {
		return Collections.unmodifiableSet(new HashSet(varToPointsToSet.keySet()));
	}

    public List<Local> getBoundLocals() {
		return new ArrayList<Local>(sootLocalToVar.keySet());
	}
	
	public boolean hasVariableMapping() {
		return varToPointsToSet!=null;
	}

	
	/**
	 * Returns the points-to set corresponding to shadow bound variable v.
	 * @param v a tracematch variable
	 * @return the points-tpo set associated with v or <code>null</code> if there is none
	 */
	public PointsToSet getPointsToSet(String v) {
		return (PointsToSet) varToPointsToSet.get(v);
	}
	
	/**
	 * @return the uniqueShadowId
	 */
	public String getUniqueShadowId() {
		return uniqueShadowId;
	}
	
	/**
	 * For a given set of {@link Shadow}s, returns the set of their
	 * unique shadow IDs.
	 * @param shadows a set of {@link Shadow}s
	 * @return the set of their shadow IDs
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public static Set uniqueShadowIDsOf(Set shadows) {
		Set ids = new HashSet();
		for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			ids.add(shadow.getUniqueShadowId());
		}
		return Collections.unmodifiableSet(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((uniqueShadowId == null) ? 0 : uniqueShadowId.hashCode());
		result = prime
				* result
				+ ((varToPointsToSet == null) ? 0 : varToPointsToSet.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Shadow other = (Shadow) obj;
		if (uniqueShadowId == null) {
			if (other.uniqueShadowId != null)
				return false;
		} else if (!uniqueShadowId.equals(other.uniqueShadowId))
			return false;
		if (varToPointsToSet == null) {
			if (other.varToPointsToSet != null)
				return false;
		} else if (!varToPointsToSet.equals(other.varToPointsToSet))
			return false;
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return uniqueShadowId + "(" + varToPointsToSet + ")";
	}

	/**
	 * @return the hasEmptyMapping
	 */
	public boolean hasEmptyMapping() {
		return hasEmptyMapping;
	}

	/**
	 * @return the container
	 */
	public SootMethod getContainer() {
		return container;
	}
	
	public TraceMatch getTraceMatch() {
		//TODO optimize by storing reference at instantiation time
		String tmName = Naming.getTracematchName(getUniqueShadowId());
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		return gai.traceMatchByName(tmName);		
	}
	
	

}
