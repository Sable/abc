package abc.weaving.aspectinfo;

import soot.*;
import abc.weaving.weaver.WeavingContext;

// FIXME
// This class is a bit of a nasty hack to allow AfterAdvice, AfterThrowingAdvice
// and AfterThrowingArgAdvice to all be handled by the AfterThrowingWeaver.
// When those weavers get moved into the advice classes, this should go away.
public interface ThrowingAdvice {
    public RefType getCatchType();

    public void bindException(WeavingContext wc,AbstractAdviceDecl ad,Local Exception);
}
