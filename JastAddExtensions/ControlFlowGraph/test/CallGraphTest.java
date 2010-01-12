public class CallGraphTest {
	public static void main(String[] args) {
//		A a = new A();
//		B b = new B();
//		a.b(0);
//		b.b(1);
		A a = new B();
		a.b(2);
	}
}

class A {
	public A() {
		this(0);
	}
	public A(int a) {
		b(a);
	}
	public void b(int a) {
	}
	public void c() {
	}
}

class B extends A {
	public B() {
		super(1);
	}
	public void b(int a) {
	}
}
