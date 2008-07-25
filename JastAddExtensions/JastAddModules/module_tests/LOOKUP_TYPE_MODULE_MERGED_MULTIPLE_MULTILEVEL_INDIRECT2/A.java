module m1;
public class A{

	m2a::B b1 = new m2a::B();
	m2b::B b2 = new m2b::B();

	m2a::m3a::C c1 = new m2a::m3a::C();
	m2a::m3b::C c2 = new m2a::m3b::C();
	m2b::m3a::C c3 = new m2b::m3a::C();
	m2b::m3b::C c4 = new m2b::m3b::C();

	m2a::m3a::m4::D d1 = new m2a::m3a::m4::D();
	m2a::m3b::m4::D d2 = new m2a::m3b::m4::D();
	m2b::m3a::m4::D d3 = new m2b::m3a::m4::D();
	m2b::m3b::m4::D d4 = new m2b::m3b::m4::D();
	m4alias1::D d5 = new m4alias1::D();
	D d6 = new D();

	public A() {
		System.out.println(this.getClass());
	}
}
