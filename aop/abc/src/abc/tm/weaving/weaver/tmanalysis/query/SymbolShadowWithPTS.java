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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.PointsToSet;
import soot.PrimType;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.sets.EqualsSupportingPointsToSet;
import soot.jimple.spark.sets.PointsToSetEqualsWrapper;
import soot.jimple.toolkits.pointer.FullObjectSet;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

/**
 * Decorator implementing a combination of a {@link SymbolShadow} with {@link PointsToSet}s.
 * Instead of a mapping from tracematch variables to {@link Local}s we
 * store a mapping from tracematch variables to the {@link PointsToSet}s
 * of those locals. 
 *
 * @author Eric Bodden
 */
public class SymbolShadowWithPTS implements ISymbolShadow {
	
	/** A mapping from each variable to its points-to sets. */
	protected Map<String,PointsToSet> varToPointsToSet;		

	/** Backwards mapping soot {@link Local}s to the tracematch variables they bind. */
	protected Map<Local,String> sootLocalToVar;		

	/** if after importing points-to sets this shadow has an empty variable-mapping, this will be set to true
	 * (can be used for error detection) */
	protected boolean hasEmptyMapping;

	/** the original symbol shadow */
	protected final ISymbolShadow symbolShadow;
    
	/**
	 * Creates a new shadow.
	 * @param symbol-shadow of a tracematch 
	 * @param container the containing method
	 */
	public SymbolShadowWithPTS(ISymbolShadow symbolShadow, SootMethod container) {
		this.symbolShadow = symbolShadow;
		this.hasEmptyMapping = false;
		//make sure the shadow was found in the method which it says that it belongs to
		assert container.equals(symbolShadow.getContainer());
		//retrieve and store points-to information
		importVariableMapping();
	}
	
	/**
	 * Imports a variable mapping into this shadow.
	 */
	private void importVariableMapping()  {
		varToPointsToSet = new HashMap();
		sootLocalToVar = new HashMap();
		
		for (String tmVar: symbolShadow.getBoundTmFormals()) {

			Local l = symbolShadow.getAdviceLocalForVariable(tmVar);
			PointsToSet pts;
			if(l.getType() instanceof PrimType) {
				//if the type of the variable is a primitive type, then we assume it could "point to anything", i.e. could have any value
				pts = FullObjectSet.v();
			} else {
				//if l is null this probably means that the WeavingVar was never woven
				assert l!=null;
				pts = (PointsToSet) Scene.v().getPointsToAnalysis().reachingObjects(l);
				//wrap in equals-wrapper
				pts = new PointsToSetEqualsWrapper( (EqualsSupportingPointsToSet) pts);
				
				if(pts.isEmpty()) {
					hasEmptyMapping = true;
					System.err.println("WARNING: Empty points-to set for variable "+l+" in "+getContainer());
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
			}
			varToPointsToSet.put(tmVar,pts);
			assert !sootLocalToVar.containsKey(l);
			sootLocalToVar.put(l, tmVar);
		}
			
	}

	/**
	 * Returns the points-to set corresponding to shadow bound variable v.
	 * @param tmVar a tracematch variable
	 * @return the points-tpo set associated with v or <code>null</code> if there is none
	 */
	public PointsToSet getPointsToSetForVariable(String tmVar) {
		return (PointsToSet) varToPointsToSet.get(tmVar);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return symbolShadow.getUniqueShadowId() + "(" + varToPointsToSet + ")";
	}

	/**
	 * @return the hasEmptyMapping
	 */
	public boolean hasEmptyMapping() {
		return hasEmptyMapping;
	}
	
	//delegate methods follow

	/**
	 * @param obj
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return symbolShadow.equals(obj);
	}

	/**
	 * @param tracematchVariable
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getAdviceLocalForVariable(java.lang.String)
	 */
	public Local getAdviceLocalForVariable(String tracematchVariable) {
		return symbolShadow.getAdviceLocalForVariable(tracematchVariable);
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getAdviceLocals()
	 */
	public Collection<Local> getAdviceLocals() {
		return symbolShadow.getAdviceLocals();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getBoundTmFormals()
	 */
	public Set<String> getBoundTmFormals() {
		return symbolShadow.getBoundTmFormals();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getContainer()
	 */
	public SootMethod getContainer() {
		return symbolShadow.getContainer();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getLocationId()
	 */
	public String getLocationId() {
		return symbolShadow.getLocationId();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getOwner()
	 */
	public TraceMatch getOwner() {
		return symbolShadow.getOwner();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getSymbolName()
	 */
	public String getSymbolName() {
		return symbolShadow.getSymbolName();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getUniqueShadowId()
	 */
	public String getUniqueShadowId() {
		return symbolShadow.getUniqueShadowId();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#hashCode()
	 */
	public int hashCode() {
		return symbolShadow.hashCode();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#isEnabled()
	 */
	public boolean isEnabled() {
		return symbolShadow.isEnabled();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow#getTmFormalToAdviceLocal()
	 */
	public Map<String, Local> getTmFormalToAdviceLocal() {
		return symbolShadow.getTmFormalToAdviceLocal();
	}

    /** 
     * {@inheritDoc}
     */
    public boolean isArtificial() {
        return symbolShadow.isArtificial();
    }	

}
