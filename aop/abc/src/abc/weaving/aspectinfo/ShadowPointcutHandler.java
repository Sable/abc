package arc.weaving.aspectinfo;

import soot.*;

/** Handler for an instance of a specific kind of shadow pointcut.
 *  Each joinpoint shadow will have one shadow type, so there will be
 *  exactly one implementation of {@link arc.weaving.aspectinfo.ShadowType}
 *  for each implementation of {@link arc.weaving.aspectinfo.ShadowPointcutHandler}.
 */
public interface ShadowPointcutHandler {
    public ShadowType getShadowType();
}
