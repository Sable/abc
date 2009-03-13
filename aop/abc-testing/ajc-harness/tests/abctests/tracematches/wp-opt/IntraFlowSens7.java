public class IntraFlowSens7 extends AbstractTest {
	
	public static void main(String[] args) {
		f1();
		f2();
		f3();
	}
	
	static IntraFlowSens7 f = new IntraFlowSens7();

	//this method itself cannot lead to a match but fragements of it can *prevent a match*
	static void f1() {
		f.a(); //this can be removed because it leads to a state which is not live
		f.x(); //needs to stay! this can discard a partial match produced by calling f3() f1() f2()
		f.b(); //we can never reach any final state when we get here -> can be removed
	}
	
	static void f2() {
		f.b();	//may lead to a complete match for "a+ b" when called in combination with f3() -> needs too stay
	}
	
	static void f3() {
		f.a();	//may lead to a complete match for "a+ b" when called in combination with f2() -> needs too stay		
	}


}