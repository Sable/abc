/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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
package abc.tmwpopt.fsanalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tmwpopt.fsanalysis.ds.Configuration;

/**
 * The current abstraction that we use for the flow-sensitive abstract interpretation of tracematches.
 * Right now it only consits of a configuration but we could add more information later on.
 */
public class Abstraction implements WorklistAnalysis.Config<Unit,Abstraction> {
	
	/** The analysis which this abstraction belongs to. */
	protected final TMWorklistBasedAnalysis analysis;

	/** The {@link Configuration} encapsulated in this abstraction. */
	protected final Configuration configuration;
	
	protected Abstraction(TMWorklistBasedAnalysis worklistBasedAnalysis, Configuration config) {
		this.analysis = worklistBasedAnalysis;
		this.configuration = config;
	}		
	
	/**
	 * @inheritDoc
	 */
	public Set<Abstraction> transition(Unit node) {
		Stmt stmt = (Stmt) node;
		
		Set<Configuration> newConfigs = new HashSet<Configuration>();
		Set<Shadow> shadows = analysis.getJob().shadowsOfStmt(stmt);

		if(shadows.isEmpty() || configuration.isTainted()) {
			//for statements without shadows or tainted statements, just copy everything over
			newConfigs.add(configuration);
		} else {		
			//otherwise compute the transition
			for(Shadow shadow: shadows) {
				Configuration newConfig = configuration.doTransition(shadow);
	            newConfigs.add(newConfig);
			}
		}

		//create the out-set and taint values in case we have side-effects
        boolean mightHaveSideEffects = mightHaveSideEffects(stmt);
        Set<Abstraction> outSet = new HashSet<Abstraction>();
        for (Configuration outConf : newConfigs) {
        	//taint outgoing value if necessary
        	if(mightHaveSideEffects) outConf = outConf.taint();
        	outSet.add(new Abstraction(analysis,outConf));
		}
        
		return outSet;
	}
	
	/**
	 * Returns <code>true</code> if we may have side-effects at the given statement,
	 * i.e. if the statement may (transitively) call an overlapping shadow.
	 */
	protected boolean mightHaveSideEffects(Stmt s) {
	    Collection<Shadow> shadows = transitivelyCalledShadows(s);
		for (Shadow shadow : shadows) {
			if(analysis.getJob().overlappingSymbolShadows().contains(shadow)) {
				return true;
			}
		}
		return false;
	}
	
    /**
	 * Returns the collection of <code>Shadow</code>s triggered in transitive callees from <code>s</code>.
	 * @param s any statement
	 */
	protected Collection<Shadow> transitivelyCalledShadows(Stmt s) {
        HashSet<Shadow> shadows = new HashSet<Shadow>();
        HashSet<SootMethod> calleeMethods = new HashSet<SootMethod>();
        LinkedList<MethodOrMethodContext> methodsToProcess = new LinkedList<MethodOrMethodContext>();

        // Collect initial edges out of given statement in methodsToProcess
        Iterator<Edge> initialEdges = analysis.getJob().abstractedCallGraph().edgesOutOf(s);
        while (initialEdges.hasNext()) {
            Edge e = initialEdges.next();
            methodsToProcess.add(e.getTgt());
            calleeMethods.add(e.getTgt().method());
        }

        // Collect transitive callees of methodsToProcess
        while (!methodsToProcess.isEmpty()) {
            MethodOrMethodContext mm = methodsToProcess.removeFirst();
            Iterator<Edge> mIt = analysis.getJob().abstractedCallGraph().edgesOutOf(mm);

            while (mIt.hasNext()) {
                Edge e = mIt.next();
                if (!calleeMethods.contains(e.getTgt().method())) {
                    methodsToProcess.add(e.getTgt());
                    calleeMethods.add(e.getTgt().method());
                }
            }
        }

        // Collect all shadows in calleeMethods
        for (SootMethod method : calleeMethods) {
        	Set<Shadow> possiblyEnabledTMShadowsOf = analysis.getJob().possiblyEnabledTMShadowsOf(method);
        	for (Shadow shadow : possiblyEnabledTMShadowsOf) {
				if(shadow.isEnabled()) {
					shadows.add(shadow);
				}
			}
        	
        }
        return shadows;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((analysis == null) ? 0 : analysis.hashCode());
		result = prime * result
				+ ((configuration == null) ? 0 : configuration.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Abstraction other = (Abstraction) obj;
		if (analysis == null) {
			if (other.analysis != null)
				return false;
		} else if (!analysis.equals(other.analysis))
			return false;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		return true;
	}
    
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Config: \n");
    	sb.append(configuration.toString());
    	sb.append("\n");
    	return sb.toString();
    }

	public Configuration getConfiguration() {
		return configuration;
	}
}