module m1;

public class A {
	public A() {
		System.out.println("create m2::B");
		m2::B b = new m2::B();
		b.f();
		m2::p.P p = new m2::p.P();
		p.f();
		System.out.println("create m2x::B");
		m2x::B b2 = new m2x::B();
		b2.f();
		m2x::p.P p2 = new m2x::p.P();
		p2.f();
		System.out.println("create m2xx::B");
		m2xx::B b3 = new m2xx::B();
		b2.f();
		m2xx::p.P p3 = new m2xx::p.P();
		p2.f();
		System.out.println(this.getClass());
	}
}
