package abc.main;

import abc.weaving.matching.*;

public class AspectJShadows {

    static void load() {
	ConstructorCallShadowType.register();
	ExecutionShadowType.register();
	GetFieldShadowType.register();
	HandlerShadowType.register();
	InitializationShadowType.register();
	MethodCallShadowType.register();
	PreinitializationShadowType.register();
	SetFieldShadowType.register();
    }
}
