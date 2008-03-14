public class IntraFlowSens2 extends AbstractTest {
	
	public static void main(String[] args) {
		IntraFlowSens2 t = new IntraFlowSens2();
		t.canMatch1();
		t.canMatch2();
		t.canMatch3();
	}
	
	void canMatch1() {
		a();
		x();
		b();
	}

	void canMatch2() {
		a();
	}

	void canMatch3() {
		b();
	}
}