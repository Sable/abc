package abc.weaving.aspectinfo;

import java.util.Hashtable;

import polyglot.util.Position;
import abc.weaving.matching.MethodPosition;
import abc.weaving.residues.*;

/** A pointcut designator that inspects a dynamic value
 *  (this,target,args)
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */

public abstract class DynamicValuePointcut extends Pointcut {
    public DynamicValuePointcut(Position pos) {
	super(pos);
    }

    // Check carefully that TargetVar, ThisVar and Args still override
    // this if you change the signature
    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	return this;
    }

}
