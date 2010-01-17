public class StaticFlowTest {
	public static void main(String[] args) {
		try {
			test1();
			test2();
		} catch (Exception e) {
		}
	}
	private static void test1() throws Exception {
		ClassLoader.getSystemClassLoader().loadClass("A");
	}
	private static void test2() throws Exception {
		ClassLoader.getSystemClassLoader().loadClass("B");
	}
}

class A {
	static {
		m();
	}
	static void m() {
	}
}
class B {
	static int a = m();
	static int m() {
		return 0;
	}
}
