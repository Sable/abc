//Listing 9.5 The subaspect

public aspect DefaultSwingThreadSafetyAspect
    extends SwingThreadSafetyAspect {
    pointcut viewMethodCalls()
	: call(* javax..JComponent+.*(..));

    pointcut modelMethodCalls()
	: call(* javax..*Model+.*(..))
	|| call(* javax.swing.text.Document+.*(..));

    pointcut uiMethodCalls()
	: viewMethodCalls() || modelMethodCalls();
}

