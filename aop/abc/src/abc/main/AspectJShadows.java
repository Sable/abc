package abc.main;

import abc.weaving.aspectinfo.*;

public class AspectJShadows {

    static void load() {
	ConstructorCall.registerShadowType();
	Execution.registerShadowType();
	GetField.registerShadowType();
	Handler.registerShadowType();
	Initialization.registerShadowType();
	MethodCall.registerShadowType();
	Preinitialization.registerShadowType();
	SetField.registerShadowType();
    }
}
