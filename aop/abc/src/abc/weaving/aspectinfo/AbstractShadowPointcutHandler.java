package abc.weaving.aspectinfo;

import soot.*;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.ShadowPointcutHandler} interface.
 *  Useful when implementing shadow pointcut handlers.
 */
public abstract class AbstractShadowPointcutHandler implements ShadowPointcutHandler {

    public ShadowType getShadowType() {
	// FIXME
	try {
	    return (ShadowType) java.lang.Class.forName(getClass().toString()+"Type").newInstance();
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
}
