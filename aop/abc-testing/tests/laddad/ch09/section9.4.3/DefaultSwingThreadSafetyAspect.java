//Listing 9.11 A subaspect that lists synchronous execution join points explicitly

public aspect DefaultSwingThreadSafetyAspect
    extends SwingThreadSafetyAspect {
    pointcut viewMethodCalls()
	: call(* javax..JComponent+.*(..));

    pointcut modelMethodCalls()
	: call(* javax..*Model+.*(..))
	|| call(* javax.swing.text.Document+.*(..));

    pointcut uiMethodCalls()
	: viewMethodCalls() || modelMethodCalls();

    pointcut uiSyncMethodCalls() :
	call(* javax..JOptionPane+.*(..))
	/* || ... */;
}
