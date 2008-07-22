module m1;

public class A {
	B b = new B();
	m2.B b2 = new m2.B();
	C c = new C();
	D d = new D();
	E e = new E();
	F f = new F();
	public A() {
		System.out.println(this.getClass());
	}
}
