
package abc.aspectj.visit;

import polyglot.frontend.*;

public class NamePatternReevaluator extends OncePass {
    public NamePatternReevaluator(Pass.ID id) {
	super(id);
    }

    public void once() {
	PatternMatcher.v().recomputeAllMatches();
    }
}
