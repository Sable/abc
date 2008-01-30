//Listing 6.10 An aspect that detects the Swing single-thread rule

import java.awt.*;
import javax.swing.JComponent;

public aspect DetectSwingSingleThreadRuleViolationAspect {
    pointcut viewMethodCalls()
	: call(* javax..JComponent+.*(..));

    pointcut modelMethodCalls()
	: call(* javax..*Model+.*(..))
	|| call(* javax.swing.text.Document+.*(..));

    pointcut uiMethodCalls()
	: viewMethodCalls() || modelMethodCalls();

    before() : uiMethodCalls() && if(!EventQueue.isDispatchThread()) {
	System.err.println(
			   "Violation: Swing method called from nonAWT thread"
			   + "\nCalled method: "
			   + thisJoinPointStaticPart.getSignature()
			   + "\nCaller: "
			   + thisEnclosingJoinPointStaticPart.getSignature()
			   + "\nSource location: "
			   + thisJoinPointStaticPart.getSourceLocation()
			   + "\nThread: " + Thread.currentThread()
			   + "\nChange code to use EventQueue.invokeLater() "
			   + "or EventQueue.invokeAndWait()\n");
    }
}
