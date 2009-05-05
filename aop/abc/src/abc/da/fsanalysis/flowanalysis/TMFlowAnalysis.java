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
package abc.da.fsanalysis.flowanalysis;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import abc.da.weaving.aspectinfo.TracePattern;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;

/**
 * Generic interface for a flow-sensitive analysis for static TracePattern optimizations.
 *
 * @author Eric Bodden
 */
public interface TMFlowAnalysis {
	
	/**
	 * @return returns the associated TracePattern
	 */
	public TracePattern tracePattern();
	
	public DirectedGraph<Unit> unitGraph();

	/**
	 * Returns the analysis job that is currently being processed.
	 */
	public AnalysisJob getJob();

	public void registerNecessaryShadow(Shadow necessaryShadow);
	
	public boolean isForward();
	
}
