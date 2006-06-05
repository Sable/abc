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
package abc.tm.weaving.aspectinfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import polyglot.util.Position;
import abc.main.Debug;
import abc.tm.ast.SymbolDecl_c;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.aspectinfo.Var;
import abc.weaving.matching.AdviceFormals;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.WeavingVar;

/**
 * @author Eric Bodden
 */
public class TMPerSymbolAdviceDecl extends TMAdviceDecl {

    /**
     * A unique id for this symbol (unique per TM decl.).
     */
    protected String symbolId;

	/**
	 * A mapping from pointcut variables to the set of weaving variables
	 * that has been used to implement those pointcut variables.
	 */
	protected Map varToWeavingVars;

    /**
     * @param symId the name of the associated symbol
     * @see TMAdviceDecl#TMAdviceDecl(AdviceSpec, Pointcut, MethodSig, Aspect, int, int, int, List, Position, String, Position, int)
     */
    public TMPerSymbolAdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl, Aspect aspct, int jp, int jpsp, int ejp, List methods, Position pos, String tm_id, Position tm_pos, String symId, int kind) {
        super(spec, pc, impl, aspct, jp, jpsp, ejp, methods, pos, tm_id, tm_pos, kind);
        symbolId = symId;
		varToWeavingVars = new HashMap();
    }
    
    /**
     * Returns the symbol id/name for which this advice is generated.
	 * @return the symbol id
	 */
	public String getSymbolId() {
		return symbolId;
	}
    
    /**
     * Returns a unique id for this symbol.
     */
    public String getQualifiedSymbolId() {
    	return SymbolDecl_c.uniqueSymbolID(tm_id, symbolId);
    }
    
    /** 
     * If we treat free variables in the static tracematch analysis, we have to
     * keep track on the variables that were woven.
     */
    public WeavingEnv getWeavingEnv() {
    	if(Debug.v().treatVariables) {
    		//create a special instance that keeps track of weaving variables
    		return new VariableTrackingAdviceFormals(this);
    	} else {
    		return new AdviceFormals(this);
    	}
    }

	/**
	 * @return
	 */
	public Map getFreeVariables() {
		removeUnwovenVariables();
		return Collections.unmodifiableMap(varToWeavingVars);
	}
	
	/**
	 * Removes variable bindings from {@link #varToWeavingVars} which refer to
	 * weaving variables that have not actually been woven.
	 */
	protected void removeUnwovenVariables() {
		//for each mapping
		for (Iterator iterator = varToWeavingVars.entrySet().iterator(); iterator.hasNext();) {
			Entry entry = (Entry) iterator.next();
			Set weavingVars = (Set) entry.getValue();
			//for each weaving var
			for (Iterator wvIter = weavingVars.iterator(); wvIter.hasNext();) {
				WeavingVar wv = (WeavingVar) wvIter.next();
				try {
					//see if it was woven
					wv.get();
				} catch(RuntimeException e) {
					//if an exception occurs, this means that no local has been
					//associated with this WeavingVar, most likely, cause
					//the WeavingVar was never actually woven;
					//we are not interested in such entries
					wvIter.remove();
				}					
			}
			if(weavingVars.isEmpty()) {
				iterator.remove();
			}
		}
	}


	/**
	 * A special weaving environment which keeps track of the
	 * weaving variables that were used.
	 * @author Eric Bodden
	 */
	protected class VariableTrackingAdviceFormals extends AdviceFormals {
    	
		/**
		 * Creates a new instance.
		 * @param ad the surrounding advice declaration
		 */
		public VariableTrackingAdviceFormals(AdviceDecl ad) {
			super(ad);
		}
    	
		/** 
		 * Keeps track of the weaving var that was used.
		 */
		public WeavingVar getWeavingVar(Var v) {
			//get the weaving var
			WeavingVar weavingVar = super.getWeavingVar(v);
			//get the current mapping
			Set weavingVars = (Set) varToWeavingVars.get(v);
			//initialize if necessary
			if(weavingVars==null) {
				weavingVars = new HashSet();
				varToWeavingVars.put(v, weavingVars);
			}
			//add the mapping
			weavingVars.add(weavingVar);
			//return
			return weavingVar;			
		}
    	
    }

}
