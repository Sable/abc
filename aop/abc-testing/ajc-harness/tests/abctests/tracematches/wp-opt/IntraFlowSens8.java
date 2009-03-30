public class IntraFlowSens8 extends AbstractTest {
	
	public static void main(String[] args) {
		f1();
	}
	
	static void f1() {
		IntraFlowSens8 f = new IntraFlowSens8();
		f.a();	//this is a prefix that is unnecessary -> can be removed
		f.x(); 	//... 
		f.a(); 
		f.b(); 
	}
	
}