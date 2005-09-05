import org.aspectj.testing.Tester;

public class IfTest {
	public static boolean aspectsEnabled = false;
	
	public static void test() {
		System.out.println("Test");
	}
	
	public static void main(String args[]){
		test();
		aspectsEnabled = true;
		test();
		Tester.checkEqual(AspectA.callCtr, 1, "AspectA.callCtr");
		Tester.checkEqual(AspectB.callCtr, 1, "AspectB.callCtr");
	}
}
