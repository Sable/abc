package abc.weaving.aspectinfo;

import polyglot.util.Position;
import abc.weaving.matching.MethodPosition;
import abc.weaving.residues.*;

/** A pointcut designator that inspects a dynamic value
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */

public abstract class DynamicValuePointcut extends AbstractPointcut {
    public DynamicValuePointcut(Position pos) {
	super(pos);
    }
}
