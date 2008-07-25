module m1;

public class A {
	BType b1 = new BType();
	m2alias::BType b2 = new m2alias::BType();

	B b3 = new B();
	m2alias::B b4 = new m2alias::B();

	m2alias::m3alias::CType c1 = new m2alias::m3alias::CType();
	m2alias::m3alias::C c2 = new m2alias::m3alias::C();

	public A() {
		System.out.println(this.getClass());
	}
}
