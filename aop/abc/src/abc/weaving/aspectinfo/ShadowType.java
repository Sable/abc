package arc.weaving.aspectinfo;

import soot.*;

/** One category of joinpoint shadows.
 *  Each joinpoint shadow will have one shadow type, so there will be
 *  exactly one implementation of {@link arc.weaving.aspectinfo.ShadowType}
 *  for each implementation of {@link arc.weaving.aspectinfo.ShadowPointcutHandler}.
 */
public interface ShadowType {

}
