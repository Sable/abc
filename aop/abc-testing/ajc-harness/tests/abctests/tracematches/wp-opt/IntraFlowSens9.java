public class IntraFlowSens9 extends AbstractTest {
	
	public static void main(String[] args) {
		f1();
	}
	
	static void f1() {
		IntraFlowSens9 f = new IntraFlowSens9();
		f.a();	
		f.x(); 	
		f.b(); 
	}
	
}