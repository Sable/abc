package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

public class ConditionPointcut extends AbstractPointcut {
    private ConditionPointcutHandler handler;

    public ConditionPointcut(ConditionPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }
}
