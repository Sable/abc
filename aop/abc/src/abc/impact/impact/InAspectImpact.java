package abc.impact.impact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Impact (shadowing and/or lookup) caused by a static crosscutting statement
 * @author Dehua Zhang
 */
public final class InAspectImpact {
	final private Set<ShadowingImpact> shadowingImpact; 
	final private Set<LookupImpact> lookupImpact;
	
	public InAspectImpact(final Set<ShadowingImpact> shadowingImpact, final Set<LookupImpact> lookupImpact) {
		if (shadowingImpact == null) this.shadowingImpact = new HashSet<ShadowingImpact>();
		else this.shadowingImpact = new HashSet<ShadowingImpact>(shadowingImpact);
		if (lookupImpact == null) this.lookupImpact = new HashSet<LookupImpact>(); 
		else this.lookupImpact = new HashSet<LookupImpact>(lookupImpact);
	}
	
	public Set<LookupImpact> getLookupImpact() {
		return Collections.unmodifiableSet(lookupImpact);
	}

	public Set<ShadowingImpact> getShadowingImpact() {
		return Collections.unmodifiableSet(shadowingImpact);
	}
}
