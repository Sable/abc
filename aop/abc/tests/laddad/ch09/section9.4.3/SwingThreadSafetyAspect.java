//Listing 9.10 The base swing thread-safety aspect: third version

import java.awt.*;
import java.util.*;
import javax.swing.*;
import pattern.worker.*;

public abstract aspect SwingThreadSafetyAspect {
    abstract pointcut uiMethodCalls();

    abstract pointcut uiSyncMethodCalls();

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

    pointcut voidReturnValueCalls()
	: call(void *.*(..));

    void around()
	: routedMethods() && voidReturnValueCalls()
	&& !uiSyncMethodCalls() {
	Runnable worker = new Runnable() {
		public void run() {
		    proceed();
		}};
	EventQueue.invokeLater(worker);
    }

    Object around()
	: routedMethods()
	&& (!voidReturnValueCalls() || uiSyncMethodCalls()) {
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
