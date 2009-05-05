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
package abc.da.fsanalysis.flowanalysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Unit;
import soot.jimple.Stmt;
import abc.da.fsanalysis.flowanalysis.ds.Configuration;

/**
 * The current abstraction that we use for the flow-sensitive abstract interpretation of TracePatterns.
 * Right now it only consists of a configuration but we could add more information later on.
 */
public class ConfigurationSet implements WorklistAnalysis.Abstraction<Unit,ConfigurationSet>, Iterable<Configuration> {
	
	/** The analysis which this abstraction belongs to. */
	protected final WorklistBasedAnalysis analysis;

	/** The configuration encapsulated in this abstraction. */
	protected final Set<Configuration> configurations;
	
	public ConfigurationSet(WorklistBasedAnalysis worklistBasedAnalysis, Set<Configuration> configurations) {
		this.analysis = worklistBasedAnalysis;
		this.configurations = configurations;
	}		
	
	/**
	 * @inheritDoc
	 */
	public ConfigurationSet transition(Unit node) {
		Stmt stmt = (Stmt) node;
		return analysis.transition(this,stmt);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((analysis == null) ? 0 : analysis.hashCode());
		result = prime * result
				+ ((configurations == null) ? 0 : configurations.hashCode());
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
		ConfigurationSet other = (ConfigurationSet) obj;
		if (analysis == null) {
			if (other.analysis != null)
				return false;
		} else if (!analysis.equals(other.analysis))
			return false;
		if (configurations == null) {
			if (other.configurations != null)
				return false;
		} else if (!configurations.equals(other.configurations))
			return false;
		return true;
	}
    
    @Override
    public String toString() {
    	return configurations.toString();
    }

	public ConfigurationSet getUnion(ConfigurationSet a) {
		if(a.analysis!=analysis) {
			throw new IllegalStateException("other abstraction is for different analysis");
		}
		Set<Configuration> union = new HashSet<Configuration>(configurations);
		union.addAll(a.getConfigurations());
		
		return new ConfigurationSet(analysis,union);
	}

	public ConfigurationSet minus(ConfigurationSet a) {
		if(a.analysis!=analysis) {
			throw new IllegalStateException("other abstraction is for different analysis");
		}
		Set<Configuration> difference = new HashSet<Configuration>(configurations);
		difference.removeAll(a.getConfigurations());
		return new ConfigurationSet(analysis,difference);
	}
	
	public boolean isEmpty() {
		return configurations.isEmpty();
	}

	public Iterator<Configuration> iterator() {
		return Collections.unmodifiableSet(configurations).iterator();
	}
	
	public Set<Configuration> getConfigurations() {
		return Collections.unmodifiableSet(configurations);
	}
	
	public void filterByImplication() {
		for (Configuration config1 : new HashSet<Configuration>(configurations)) {
			for (Iterator<Configuration> iterator = configurations.iterator(); iterator.hasNext();) {
				Configuration config2 = iterator.next();
				if(config1!=config2 && config1.implies(config2)) {
					iterator.remove();
				}
			}
		}
	}

}