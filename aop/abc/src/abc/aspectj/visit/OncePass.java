
package abc.aspectj.visit;

import polyglot.frontend.*;

import java.util.*;

public abstract class OncePass extends AbstractPass {

    private static Set has_been_run = new HashSet();

    public static void reset() {
	has_been_run = new HashSet();
    }

    public OncePass(Pass.ID id) {
	super(id);
    }

    public final boolean run() {
	if (!has_been_run.contains(id())) {
	    once();
	    has_been_run.add(id());
	}
	return true;
    }

    protected abstract void once();
}
