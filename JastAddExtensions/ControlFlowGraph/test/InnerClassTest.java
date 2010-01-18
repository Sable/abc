public class InnerClassTest {
	public static void main(String[] args) {
		test1();
		test2();
	}
	private static void test1() {
		A a = new A();
		a.m();
	}
	private static void test2() {
		A a = new A();
		a.n();
	}
}

class A {
	void m() {
		class LocalA {
			void localM() {
				B b = new B();
				b.m();
			}
		}
		LocalA a = new LocalA();
		a.localM();
	}

	void n() {
		InnerA a = new InnerA(); 
		a.innerM();
	}

	class InnerA {
		void innerM() {
		}
	}
}

class B {
	void m() {
	}
}

