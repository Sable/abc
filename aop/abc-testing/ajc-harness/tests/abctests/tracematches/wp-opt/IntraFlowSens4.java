public class IntraFlowSens4 extends AbstractTest {
	static IntraFlowSens4 t1 = new IntraFlowSens4();
	
	public static void main(String[] args) {
		canMatch1();
	}
	
	static void canMatch1() {
		t1.a();
		t1.x();
		foo();
		t1.b();
	}
	
	static void foo() {
		t1.a();
	}
}