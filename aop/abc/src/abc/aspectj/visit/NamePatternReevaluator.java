
package abc.aspectj.visit;

import polyglot.frontend.*;

public class NamePatternReevaluator extends AbstractPass {
    private boolean has_been_run = false;

    public NamePatternReevaluator(Pass.ID id) {
	super(id);
    }

    public boolean run() {
	if (!has_been_run) {
	    PatternMatcher.v().recomputeAllMatches();
	    has_been_run = true;
	}
	return true;
    }
}
