package abc.weaving.aspectinfo;

import polyglot.util.Position;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.Pointcut} 
 *  interface.
 *  Useful when implementing new kinds of poincuts.
 */
public abstract class AbstractPointcut extends Syntax implements Pointcut {
    public AbstractPointcut(Position pos) {
	super(pos);
    }

    /** Force subclasses to define toString */
    public abstract String toString();
}
