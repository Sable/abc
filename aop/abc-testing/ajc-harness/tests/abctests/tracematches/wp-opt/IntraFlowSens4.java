public class IntraFlowSens4 extends AbstractTest {
	static IntraFlowSens4 t1 = new IntraFlowSens4();
	
	public static void main(String[] args) {
		canMatch1();
	}
	
	static void canMatch1() {
		t1.a();		//can be removed: match produced by "a" is certainly discarded by the following "x"
		t1.x();		//cannot! be removed (consider call sequence foo(), canMatch1() with first shadow removed, and foo() virtually dispatched to a version where a() is never called)
		foo();
		t1.b();
	}
	
	static void foo() {
		t1.a();
	}
}