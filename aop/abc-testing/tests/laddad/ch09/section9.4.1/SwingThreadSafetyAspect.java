//Listing 9.4 The base swing thread-safety aspect: First version

import java.awt.*;
import java.util.*;
import javax.swing.*;
import pattern.worker.*;

public abstract aspect SwingThreadSafetyAspect {
    abstract pointcut uiMethodCalls();

    pointcut threadSafeCalls()
	: call(void JComponent.revalidate())
	|| call(void JComponent.repaint(..))
	|| call(void add*Listener(EventListener))
	|| call(void remove*Listener(EventListener));

    pointcut excludedJoinpoints()
	: threadSafeCalls()
	|| within(SwingThreadSafetyAspect)
	|| if(EventQueue.isDispatchThread());

    pointcut routedMethods()
	: uiMethodCalls() && !excludedJoinpoints();

    Object around() : routedMethods() {
	RunnableWithReturn worker = new RunnableWithReturn() {
		public void run() {
		    _returnValue = proceed();
		}};
	try {
	    EventQueue.invokeAndWait(worker);
	} catch (Exception ex) {
	    // ... log exception
	    return null;
	}
	return worker.getReturnValue();
    }
}

