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
package abc.tmwpopt.fsanalysis.mustalias;

import java.util.Map;

import soot.Local;
import soot.jimple.toolkits.pointer.InstanceKey;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tmwpopt.fsanalysis.stages.AnalysisJob;

/**
 * Generic interface for a flow-sensitive analysis for static tracematch optimizations.
 *
 * @author Eric Bodden
 */
public interface TMFlowAnalysis {
	
	/**
	 * @return returns the associated tracematch
	 */
	public TraceMatch getTracematch();
        
    /**
     * notifies the analysis that a final state was hit. 
     */
    public void hitFinal();

	/**
	 * Converts a mapping from tracematch formals to advice locals to
	 * a mapping from tracematch formals to instance keys.
	 */
	public Map<String,InstanceKey> reMap(Map<String, Local> tmFormalToAdviceLocal);

	/**
	 * Returns the analysis job that is currently being processed.
	 */
	public AnalysisJob getJob();

}
