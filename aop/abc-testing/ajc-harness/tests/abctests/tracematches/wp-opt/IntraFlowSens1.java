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
		a();	//can match -> must stay
		b();	//can match -> must stay
	}

	void canMatch2() {
		a();	//can match -> must stay
		if(System.currentTimeMillis()>1021231)
			b();//can match -> must stay
	}

	void canMatch3() {
		a();//can match -> must stay
		if(System.currentTimeMillis()>1021231)
			x();	//can discard a match -> must stay
		b();//can match -> must stay
	}
	
	void canMatch4() {
		b();//can lead to a match in combination with a call to canMatch2() -> must stay
	}

	void cannotMatch() {	//cannot match itself but discard a match
		a();	//is on a dead path -> can be removed
		x();	//needs to stay because it can discard a partial match, e.g. canMatch2() cannotMatch() canMatch4()
		b();	//can be removed (dead)
	}

	void cannotMatch2() {
		x();	//needs to stay because it can discard a partial match, e.g. canMatch2() cannotMatch2() canMatch4()
		b();	//can be removed (dead)
	}

	void unnecessary() {
		a();//one of those
		a();//is unnecessary
		b();	//can match -> must stay
	}
	
	void canMatchLoop() {
		while(a()) {	//can match -> must stay
			x();		//unnecessary
		}
		b();			//can match -> must stay
	}
	
	void cannotMatchLoop() {
		while(x()) {	//needs to stay because it can discard a partial match, e.g. canMatch2() cannotMatchLoop() canMatch4()
			a();		//is on a dead path -> can be removed
		}
		b();			//is dead -> can be removed
	}
	
	void cannotMatchFinally() {
		try {
			a();	//is on a dead path -> can be removed
		} finally {
			x();	//needs to stay because it can discard a partial match, e.g. canMatch2() cannotMatchFinally() canMatch4()
		}
		b();		//is dead -> can be removed
	}
	
	void canMatchTryCatch() {
		try {
			a();	//can match -> must stay
		} catch(RuntimeException e) {
			x();	//can discard a match -> must stay
		}
		b();		//can match -> must stay
	}
}