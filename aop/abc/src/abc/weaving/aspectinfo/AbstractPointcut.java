package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

public abstract class AbstractPointcut extends Syntax implements Pointcut {
    public AbstractPointcut(Position pos) {
	super(pos);
    }
}
