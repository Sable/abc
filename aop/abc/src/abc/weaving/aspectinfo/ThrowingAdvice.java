package abc.weaving.aspectinfo;

import soot.*;
import abc.weaving.weaver.WeavingContext;

public interface ThrowingAdvice {
    public RefType getCatchType();

    public void bindException(WeavingContext wc,AbstractAdviceDecl ad,Local Exception);
}
