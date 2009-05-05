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
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.toolkits.graph.DirectedGraph;


/**
 * Abstract superclass for worklist based analyses.
 *
 * @param <N> node type of the {@link DirectedGraph} that is analyzed
 * @param <A> the configuration type
 * @author Eric Bodden
 */
public abstract class WorklistAnalysis<N,A extends WorklistAnalysis.Abstraction<N,A>> {

	/**
	 * Abstract interface for analysis configurations.
	 *
	 * @param <N> node type of the {@link DirectedGraph} that is analyzed
	 * @param <A> the configuration type
	 * @author Eric Bodden
	 */
	public interface Abstraction<N,A>{
		
		/**
		 * Computes a transition over the given nodes, returning the set of successor configurations.
		 */
		A transition(N node);
		
		A getUnion(A a);
		
		A minus(A a);
		
		boolean isEmpty();
		
		void filterByImplication();
	}

	protected JobPool worklist;
	
	protected final DirectedGraph<N> graph;
	
	protected IdentityHashMap<N,A> nodeToBeforeFlow, nodeToAfterFlow;

	protected int jobCount;

	public WorklistAnalysis(DirectedGraph<N> graph) {
		this.graph = graph;	
		this.worklist = new JobPool();
		this.nodeToBeforeFlow = new IdentityHashMap<N,A>();
		this.nodeToAfterFlow = new IdentityHashMap<N,A>();
	}

	protected void doAnalysis() {
		if(worklist==null) {
			throw new IllegalStateException("already run!");
		}
		
		Set<Job<N, A>> initialJobs = initialJobs();
		checkInitialJobs(initialJobs);
		worklist.addAll(initialJobs);
		
		while(!worklist.isEmpty()) {
			//pop first element
			Job<N, A> job = worklist.pop();
			jobCount++;
			
			N node = job.getNode();
			A oldFlowBefore = getFlowBefore(node);
			A toCompute = job.getAbstraction();
			toCompute = toCompute.getUnion(oldFlowBefore);
			toCompute.filterByImplication();
			
			A configsNotSeenBefore = toCompute.minus(oldFlowBefore);
			
			if(!configsNotSeenBefore.isEmpty()) {
				A succConfigs = configsNotSeenBefore.transition(node);
				setFlowBefore(node, configsNotSeenBefore.getUnion(oldFlowBefore));
				
				A oldFlowAfter = getFlowAfter(node);
				A newConfigsComputed = succConfigs.minus(oldFlowAfter);
				if(!newConfigsComputed.isEmpty()) {
					setFlowAfter(node, succConfigs.getUnion(oldFlowAfter));
					for (N succ : graph.getSuccsOf(node)) {
						worklist.add(new Job<N,A>(succ,newConfigsComputed));
					}
				}
			}
		}		
		
		worklist = null;
	}
	
	protected abstract A initialAbstraction();

	protected abstract void checkInitialJobs(Set<Job<N, A>> initialJobs);

	private void setFlowBefore(N node, A config) {
		nodeToBeforeFlow.put(node, config);
	}
	
	private void setFlowAfter(N node, A config) {
		nodeToAfterFlow.put(node, config);
	}
	
	public A getFlowBefore(N node) {
		A a = nodeToBeforeFlow.get(node);
		if(a==null) {
			a = initialAbstraction();
			nodeToBeforeFlow.put(node, a);
		}
		return a;
	}

	public A getFlowAfter(N node) {
		A a = nodeToAfterFlow.get(node);
		if(a==null) {
			a = initialAbstraction();
			nodeToAfterFlow.put(node, a);
		}
		return a;
	}

	protected abstract Set<Job<N,A>> initialJobs();
	
	protected static class Job<N,A> {
		private final N node;
		private final A abstraction;

		public Job(N node, A config) {
			super();
			this.node = node;
			this.abstraction = config;
		}
	
		public N getNode() {
			return node;
		}

		public A getAbstraction() {
			return abstraction;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((abstraction == null) ? 0 : abstraction.hashCode());
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
			if (abstraction == null) {
				if (other.abstraction != null)
					return false;
			} else if (!abstraction.equals(other.abstraction))
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
			b.append("\nabstraction:\n");
			b.append(abstraction.toString());
			b.append("\n");
			return b.toString();
		}
	}

	/**
	 * Returns the number os jobs successfully processed.
	 */
	public int getJobCount() {
		return jobCount;
	}
	
	
	protected class JobPool {

		private Map<N,A> mapping = new HashMap<N, A>();
		
		public void add(Job<N,A> job) {
			N node = job.getNode();
			A abstractionForNode = mapping.get(node);
			if(abstractionForNode==null) {
				mapping.put(node,job.getAbstraction());
			} else {
				mapping.put(node,job.getAbstraction().getUnion(abstractionForNode));
			}
		}
		
		public void addAll(Collection<Job<N,A>> jobs) {
			for (Job<N, A> job : jobs) {
				add(job);
			}
		}
		
		public boolean isEmpty() {
			return mapping.isEmpty();
		}
		
		public Job<N,A> pop() {
			if(mapping.isEmpty()) {
				throw new IllegalStateException("no jobs left");
			} else {
				Iterator<Entry<N, A>> entryIter = mapping.entrySet().iterator();
				Entry<N, A> someEntry = entryIter.next();
				entryIter.remove();
				return new Job<N, A>(someEntry.getKey(),someEntry.getValue());
			}
		}
	}
}
