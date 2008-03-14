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
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import soot.toolkits.graph.DirectedGraph;


/**
 * Abstract superclass for worklist based analyses.
 *
 * @param <N> node type of the {@link DirectedGraph} that is analyzed
 * @param <C> the configuration type
 * @author Eric Bodden
 */
public abstract class WorklistAnalysis<N,C extends WorklistAnalysis.Config<N,C>> {
	
	public static class TimeOutException extends Exception {

		private static final long serialVersionUID = 1L;
		private final int fullJobCount;

		public TimeOutException(int fullJobCount) {
			this.fullJobCount = fullJobCount;
		}
		
		@Override
		public String getMessage() {
			return "Fixed point iteration was terminated after it was determined that at least " +
			+fullJobCount+" jobs would have to be processed.";
		}

	}

	/**
	 * Abstract interface for analysis configurations.
	 *
	 * @param <N> node type of the {@link DirectedGraph} that is analyzed
	 * @param <C> the configuration type
	 * @author Eric Bodden
	 */
	public interface Config<N,C> {
		
		/**
		 * Computes a transition over the given nodes, returning the set of successor configurations.
		 */
		Collection<C> transition(N node);
	}

	protected Set<Job<N,C>> worklist;
	
	protected final DirectedGraph<N> graph;
	
	protected IdentityHashMap<N,Set<C>> nodeToBeforeFlow, nodeToAfterFlow;

	protected final boolean computeBeforeFlow;
	
	protected int jobCount;

	protected final int MAX_JOB_COUNT;
	
	public WorklistAnalysis(DirectedGraph<N> graph, int maxJobCount) {
		this(graph,false,maxJobCount);
	}

	public WorklistAnalysis(DirectedGraph<N> graph, boolean computeBeforeFlow, int maxJobCount) {
		this.graph = graph;
		this.computeBeforeFlow = computeBeforeFlow;
		this.MAX_JOB_COUNT = maxJobCount;		
	}

	protected void doAnalysis() throws TimeOutException {		
		worklist = new HashSet<Job<N,C>>();
		if(computeBeforeFlow) {
			nodeToBeforeFlow = new IdentityHashMap<N,Set<C>>();
		}
		nodeToAfterFlow = new IdentityHashMap<N,Set<C>>();
		
		Set<C> initialConfigs = Collections.unmodifiableSet(initialConfigurations());
		for (N node : graph.getHeads()) {
			worklist.add(new Job<N,C>(node,initialConfigs));
		}
		
		while(!worklist.isEmpty()) {
			int fullJobCount = worklist.size()+jobCount;
			if(fullJobCount > MAX_JOB_COUNT) {
				throw new TimeOutException(fullJobCount);
			}
			
			//pop first element
			Iterator<Job<N, C>> iterator = worklist.iterator();
			Job<N, C> job = iterator.next();
			//the following will usually bail in case we got hash codes wrong
			//due to destructive updates
			assert worklist.contains(job);
			if(!worklist.contains(job)) {				
				throw new RuntimeException();
			}
			iterator.remove();
			jobCount++;
			
			N node = job.getNode();

			Set<C> newConfigs = new HashSet<C>();
			for (C config : job.getConfigs()) {
				Collection<C> succConfigs = config.transition(node);
				for (C succConfig : succConfigs) {
					Set<C> flowAfter = getFlowAfter(node);
					if(!flowAfter.contains(succConfig)) {
						newConfigs.add(succConfig);
						flowAfter.add(succConfig);
					}
				}
			}
			if(!newConfigs.isEmpty()) {
				for (N succ : graph.getSuccsOf(node)) {
					worklist.add(new Job<N,C>(succ,newConfigs));
					if(computeBeforeFlow) {
							getFlowBefore(succ).addAll(newConfigs);
					}
				}
			}
		}		
		
		worklist = null;
	}

	public Set<C> getFlowBefore(N node) {
		if(computeBeforeFlow) {
			Set<C> set = nodeToBeforeFlow.get(node);
			if(set==null) {
				set = new HashSet<C>();
				nodeToBeforeFlow.put(node, set);
			}
			return set;
		} else {
			throw new IllegalStateException("No before-flow computed! Check constructor parameters!");
		}
	}

	public Set<C> getFlowAfter(N node) {
		Set<C> set = nodeToAfterFlow.get(node);
		if(set==null) {
			set = new HashSet<C>();
			nodeToAfterFlow.put(node, set);
		}
		return set;
	}

	protected abstract Set<C> initialConfigurations();
	
	protected static class Job<N,C> {
		private final N node;
		private final Set<C> configs;

		private Job(N node, Set<C> config) {
			super();
			this.node = node;
			this.configs = config;
		}
	
		public N getNode() {
			return node;
		}

		public Set<C> getConfigs() {
			return configs;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((configs == null) ? 0 : configs.hashCode());
			result = prime * result + ((node == null) ? 0 : node.hashCode());
			return result;
		}


		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Job other = (Job) obj;
			if (configs == null) {
				if (other.configs != null)
					return false;
			} else if (!configs.equals(other.configs))
				return false;
			if (node == null) {
				if (other.node != null)
					return false;
			} else if (!node.equals(other.node))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			StringBuffer b = new StringBuffer();
			b.append("node: \n");
			b.append(node);
			b.append("\nconfigurations:\n");
			for (C c : configs) {
				b.append(c.toString());
				b.append("\n");
			}
			return b.toString();
		}
	}

	/**
	 * Returns the number os jobs successfully processed.
	 */
	public int getJobCount() {
		return jobCount;
	}
}
