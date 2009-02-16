package abc.impact.impact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Impact (computation and state) caused by an advice 
 * @author Dehua Zhang
 *
 */
public final class AdviceImpact {
	final private Set<StateImpact> stateImpactSet;
	final private ComputationImpact computationImpact;
	
	public AdviceImpact(final Set<StateImpact> stateImpact, final ComputationImpact computationImpact) {
		if (stateImpact == null) this.stateImpactSet = new HashSet<StateImpact>();
		else this.stateImpactSet = new HashSet<StateImpact>(stateImpact);
		this.computationImpact = computationImpact;
	}

	public ComputationImpact getComputationImpact() {
		return computationImpact;
	}

	public Set<StateImpact> getStateImpact() {
		return Collections.unmodifiableSet(stateImpactSet);
	}
}
