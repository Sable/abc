package abc.weaving.aspectinfo;

import soot.*;

import java.util.*;

/** Handler for <code>args</code> condition pointcut. */
public class Args extends AbstractOtherPointcutHandler {
    private List/*<ArgPattern>*/ args;

    /** Create an <code>args</code> pointcut.
     *  @param args a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public Args(List args) {
	this.args = args;
    }

    /** Get the list of argument patterns.
     *  @return a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public List getArgs() {
	return args;
    }
}
