package abc.main;

import abc.weaving.matching.*;

public class AJShadows {

    static void load() {
	ConstructorCallShadowType.register();
	ExecutionShadowType.register();
	GetFieldShadowType.register();
	HandlerShadowType.register();
	ClassInitializationShadowMatch.register();
	InterfaceInitializationShadowMatch.register();
	MethodCallShadowType.register();
	PreinitializationShadowType.register();
	SetFieldShadowType.register();
    }
}
