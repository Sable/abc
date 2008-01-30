import org.aspectj.testing.Tester;

public class Throw
{
	public static void main(String[] args) {
		try {
			willThrow(true);
			Tester.checkFailed("should have thrown exception");
		} catch (RuntimeException re) {
			Tester.checkEqual("expected exception", re.getMessage());
		}
		Tester.checkEvents(new String[] 
			{ "before throw", "after throwing throw", "after throw" });
	}
    
	static void willThrow(boolean shouldThrow) {
		int x;
		if (shouldThrow) throw new RuntimeException("expected exception");
		else x = 42;
		System.out.println("x = " + x);
	}
}

aspect A {    
	before(Throwable t): throw() && args(t) {
		Tester.event("before throw");
		Tester.checkEqual(thisJoinPoint.getSignature().toShortString(), "throw");
		Tester.checkEqual(t.getMessage(), "expected exception");
	}
    
	after() returning: throw() {
		Tester.checkFailed("shouldn't ever return normally from a throw");
	}
    
	after() throwing(RuntimeException re): throw() {
		Tester.event("after throwing throw");
		Tester.checkEqual(re.getMessage(), "expected exception");
	}
    
	after(): throw() {
		Tester.event("after throw");
	}
	
	void around(): throw() { } // around advice on throw does not apply
}