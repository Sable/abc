package abc.weaving.weaver;

import soot.Value;

/** Somewhere to put a single value in the weaving context
 *  (used for 'perthis' and pertarget' setup)
 *  @author Ganesh Sittampalam
 */

public class SingleValueWeavingContext extends WeavingContext {
    public Value value=null;

}
