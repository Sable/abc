
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An argument pattern denoting a pointcut variable. */
public class ArgVar extends ArgPattern {
    private Var var;

    public ArgVar(Var var, Position pos) {
	super(pos);
	this.var = var;
    }

    public Var getVar() {
	return var;
    }

}
