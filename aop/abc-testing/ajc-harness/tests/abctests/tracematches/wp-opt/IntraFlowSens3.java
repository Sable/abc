public class IntraFlowSens3 extends AbstractTest {
	static IntraFlowSens3 t1 = new IntraFlowSens3();
	static IntraFlowSens3 t2 = new IntraFlowSens3();
	
	public static void main(String[] args) {
		canMatch1();
		canMatch2();
		canMatch3();
		canMatch4();
	}
	
	static void canMatch1() { //cannot match
		t1.a();//can go
		t1.x();//must stay because it could discard a partial match that started earlier
		t1.b();//can go
	}

	static void canMatch2() {
		t1.a();//must stay
	}

	static void canMatch3() {
		t1.b();//must stay
	}
	
	static void canMatch4() {
		t1.a();//can go
		t1.x();//must stay because it could discard a partial match that started earlier
		t1.b();//can go
		t2.a();//field (!) t2 is set in this method, so we only have weak information for t2
		t2 = create();	//don't know what's assigned here; could or could not be the same as the previous object
		t2.x();//and hence we have to keep...
		t2.b();//these guys alive
	}
	
	static IntraFlowSens3 create() {
		return null;
	}

}