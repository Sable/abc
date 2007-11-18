public class IntraFlowSens1 extends AbstractTest {
	
	public static void main(String[] args) {
		IntraFlowSens1 t = new IntraFlowSens1();
		t.canMatch1();
		t.canMatch2();
		t.canMatch3();
		t.canMatch4();
		t.cannotMatch();
		t.cannotMatch2();
		t.unnecessary();
		t.canMatchLoop();
		t.cannotMatchLoop();
		t.cannotMatchFinally();
		t.canMatchTryCatch();
	}
	
	void canMatch1() {
		a();
		b();
	}

	void canMatch2() {
		a();
		if(System.currentTimeMillis()>1021231)
			b();
	}

	void canMatch3() {
		a();
		if(System.currentTimeMillis()>1021231)
			x();
		b();
	}
	
	void canMatch4() {
		b();
	}

	void cannotMatch() {
		a();
		x();
		b();
	}

	void cannotMatch2() {
		x();
		b();
	}

	void unnecessary() {
		a();//one of those
		a();//is unnecessary, but we currently do not detect this :-(
		b();
	}
	
	void canMatchLoop() {
		while(a()) {
			x();//is unnecessary
		}
		b();
	}
	
	void cannotMatchLoop() {
		while(x()) {
			a();
		}
		b();
	}
	
	void cannotMatchFinally() {
		try {
			a();
		} finally {
			x();
		}
		b();
	}
	
	void canMatchTryCatch() {
		try {
			a();
		} catch(RuntimeException e) {
			x();
		}
		b();
	}
}