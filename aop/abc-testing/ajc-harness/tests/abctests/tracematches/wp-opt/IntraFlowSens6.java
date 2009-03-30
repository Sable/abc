public class IntraFlowSens6 extends AbstractTest {
	
	public static void main(String[] args) {
		f1();
		f2();
		f3();
	}
	
	static void f1() {
		IntraFlowSens6 t = new IntraFlowSens6();
		while(t.x()) { 	//can never match the pattern "a a" over {a,x} -> removed
			t.a();		//can never match the pattern "a a" over {a,x} -> removed
		}
	}
	
	static IntraFlowSens6 f = new IntraFlowSens6();

	static void f2() {
		while(f.x()) {	//could prevent a match when called between two calls to f3 -> must stay
			f.a();		//can never match the pattern "a a" over {a,x} -> can be removed
		}
	}
	
	static void f3() {
		f.a();	//could lead to a match for "a a" if called twice -> needs to stay
	}

}