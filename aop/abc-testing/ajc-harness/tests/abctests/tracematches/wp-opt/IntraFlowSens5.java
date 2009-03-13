public class IntraFlowSens5 extends AbstractTest {
	
	public static void main(String[] args) {
		f1();
		f2();
	}
	
	static void f1() {
		IntraFlowSens5 t = new IntraFlowSens5();
		t.a();
		if(t.hashCode()==1) { //some predicate that cannot be statically evaluated
			t.x();			
		}
		t.b();
	}
	
	static void f2() {
		IntraFlowSens5 t = new IntraFlowSens5();
		t.a();
		if(t.hashCode()==1) { //some predicate that cannot be statically evaluated
			t.x();			
		} else {
			t.x();			
		}
		t.b();
	}
	
}