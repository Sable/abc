package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

public class ShadowPointcut extends AbstractPointcut {
    private ShadowPointcutHandler handler;

    public ShadowPointcut(ShadowPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }
}
