module m1;

public class A {
	m2::B b = new m2::B();
	m2x::B b2 = new m2x::B();
	m2x::BX bx = new m2x::BX();
	public A() {
		System.out.println(this.getClass());
	}
}
