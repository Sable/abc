package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

/** Handler for <code>execution</code> shadow pointcut with a method pattern. */
public class Execution extends AbstractShadowPointcutHandler {
    public boolean matchesAt(Stmt stmt) {
	return stmt==null;
    }

    public String toString() {
	return "execution()";
    }

}
