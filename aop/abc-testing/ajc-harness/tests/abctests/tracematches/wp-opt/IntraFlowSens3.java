public class IntraFlowSens3 extends AbstractTest {
	static IntraFlowSens3 t1 = new IntraFlowSens3();
	static IntraFlowSens3 t2 = new IntraFlowSens3();
	
	public static void main(String[] args) {
		canMatch1();
		canMatch2();
		canMatch3();
		canMatch4();
	}
	
	static void canMatch1() {
		t1.a();
		t1.x();
		t1.b();
	}

	static void canMatch2() {
		t1.a();
	}

	static void canMatch3() {
		t1.b();
	}
	
	static void canMatch4() {
		t1.a();
		t1.x();
		t1.b();
		t2.a();
		t2.x();
		t2 = new IntraFlowSens3();
		t2.b();
	}

}