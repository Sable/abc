//Listing 9.8 The base swing thread-safety aspect: second version

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

    pointcut voidReturnValueCalls()
	: call(void *.*(..));

    Object around()
	: routedMethods() && !voidReturnValueCalls() {
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

    void around()
	: routedMethods() && voidReturnValueCalls() {
	Runnable worker = new Runnable() {
		public void run() {
		    proceed();
		}};
	EventQueue.invokeLater(worker);
    }
}
