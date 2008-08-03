module m1;
public class A {
	B b = new B();
	C c = new C();
	m2::m4::D d1 = new D();
	m3::m4::D d2 = new D();
	m4::D d3 = new m4::D();
	public A() {
		System.out.println(this.getClass());
	}
}
