package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Skeleton implementation of the {@link arc.weaving.aspectinfo.Per} interface.
 *  Useful when implementing per clauses.
 */
public abstract class AbstractPer extends Syntax implements Per {
    public AbstractPer(Position pos) {
	super(pos);
    }
}
