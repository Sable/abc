jpi void MethodCall(AbstractCommand ac);

abstract class AbstractCommand{
	public void exec(){}
}

class UndoableCommand extends AbstractCommand{}

class UndoCommand extends AbstractCommand{
	
	exhibits void MethodCall(AbstractCommand ac) :
		execution(void exec()) && this(ac);
	
	@Override
	public void exec(){}	
}

public aspect TypeVariance{
	
	void around MethodCall(AbstractCommand ac){
		proceed(new UndoableCommand());
	}
	
	public static void main(String[] args){
		UndoCommand uc = new UndoCommand();
		uc.exec();		
	}
}


Running test 1655: abctests/jpi/Type Variance
Commandline: abc -d abctests/jpi -warn-unused-advice:off -ext abc.ja.jpi abctests/jpi/TypeVariance.java 
ac
InvocationTargetException while trying to run compiled class: java.lang.ClassCastException: UndoableCommand cannot be cast to UndoCommand
java.lang.reflect.InvocationTargetException
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at java.lang.reflect.Method.invoke(Method.java:597)
	at abc.testing.TestCase.runTest(TestCase.java:413)
	at abc.testing.Main.doCase(Main.java:288)
	at abc.testing.Main.main(Main.java:122)
Caused by: java.lang.ClassCastException: UndoableCommand cannot be cast to UndoCommand
	at TypeVariance.inline$0$around$0(TypeVariance.java:23)
	at UndoCommand.exec(TypeVariance.java)
	at TypeVariance.main(TypeVariance.java:28)
	... 7 more
FAIL: Test 1655: "abctests/jpi/Type Variance" failed in 7019ms, memory usage: 4512216.