public class InstanceFlowTest {
	public static void main(String[] args) {
		test1();
	}
	// TODO: The constructor call should appear as a callee
	private static void test1() {
		A a = new A();
	}
}
class A {

	{
		m();
	}
	int a = n();
	{
		m();
	}

	public A() {
		this(1);
	}
	public A(int a) {
		this.a = a;
	}

	void m() {
	}
	int n() {
		return 0;
	}
}
