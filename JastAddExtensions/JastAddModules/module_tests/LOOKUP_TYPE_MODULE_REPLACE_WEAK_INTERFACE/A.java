module m1;
public class A {
	B b = new B();
	C c = new C();
	m2::m4::d.D d1 = new d.D();
	m3::m4::d.D d2 = new d.D();
	m4::d.D d3 = new m4::d.D();

	m2::m4::dd.DD d4 = new dd.DD();
	m3::m4::dd.DD d5 = new dd.DD();
	dd.DD d6 = new dd.DD();
	public A() {
		System.out.println(this.getClass());
	}
}
