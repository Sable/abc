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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.RefLikeType;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.InstanceKey;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tmwpopt.fsanalysis.ds.Configuration;
import abc.tmwpopt.fsanalysis.ds.Constraint;
import abc.tmwpopt.fsanalysis.ds.Disjunct;
import abc.tmwpopt.fsanalysis.ds.PreciseSymmetricDisjunct;
import abc.tmwpopt.fsanalysis.mustalias.InstanceKeyNonRefLikeType;
import abc.tmwpopt.fsanalysis.mustalias.TMFlowAnalysis;
import abc.tmwpopt.fsanalysis.stages.AnalysisJob;

/**
 * A worklist algorithm for the flow-sensitive abstract interpretation of tracematches.
 * The algorithm exhaustively generates all possible automaton configurations at all statements
 * in a given method body. It is parameterized by an {@link AnalysisJob}.
 */
public class TMWorklistBasedAnalysis extends WorklistAnalysis<Unit, Abstraction> implements TMFlowAnalysis {

	/**
	 * The analysis job that parameterizes this analysis.
	 */
	protected AnalysisJob job;
	
	/**
	 * Creates a new analysis.
	 * @param job the analysis job to process
	 * @param maxJobCount the maximal numbers of jobs to process from the worklist
	 * @throws TimeOutException thrown if maxJobCount would needed to be processed to analyze the given method to completion
	 */
	public TMWorklistBasedAnalysis(AnalysisJob job, int maxJobCount) throws TimeOutException {
		super(job.unitGraph(),maxJobCount);
		this.job = job;
		
		Disjunct<InstanceKey> initialDisjunct = new PreciseSymmetricDisjunct(job);
		initialDisjunct.setFlowAnalysis(this);
		Constraint.initialize(initialDisjunct);
		
		doAnalysis();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected Set<Abstraction> initialConfigurations() {
        Set<Abstraction> compoundConfigs = new HashSet<Abstraction>();
        for (Iterator<State> stateIter = job.stateMachine().getStateIterator(); stateIter.hasNext();) {
            State state = stateIter.next();
            if(!state.isFinalNode()) {

                Configuration entryInitialConfiguration = new Configuration(
                		this,
                		Collections.singleton(state),
                		false //count final-hits, if not aborting when hitting final
                );

                compoundConfigs.add(new Abstraction(this, entryInitialConfiguration));
            }
        }
		return compoundConfigs;
	}
	
	/**
	 * @inheritDoc
	 */
	public Map<String, InstanceKey> reMap(Map<String, Local> bindings) {
        Map<String,Local> origBinding = bindings;
        Map<String,InstanceKey> newBinding = new HashMap<String, InstanceKey>();
        for (Map.Entry<String,Local> entry : origBinding.entrySet()) {
            String tmVar = entry.getKey();
            Local adviceLocal = entry.getValue();
            Stmt stmt = job.defStmtOf(adviceLocal); //may be null, if adviceLocal is not part of this method
            InstanceKey instanceKey = (adviceLocal.getType() instanceof RefLikeType) ?
              new InstanceKey(adviceLocal,stmt,job.method(),job.localMustAliasAnalysis(),job.localNotMayAliasAnalysis()) :
              new InstanceKeyNonRefLikeType(adviceLocal,stmt,job.method(),job.localMustAliasAnalysis(),job.localNotMayAliasAnalysis());
            newBinding.put(tmVar, instanceKey);
        }
        return newBinding;
	}

	/**
	 * @inheritDoc
	 */
	public AnalysisJob getJob() {
		return job;
	}

	/**
	 * @inheritDoc
	 */
	public TraceMatch getTracematch() {
		return job.traceMatch();
	}

	/**
	 * @inheritDoc
	 */
	public void hitFinal() {
		//not needed right now
	}
}
