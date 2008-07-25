module m0;
public class Z{

	A a1 = new A();
	m1::A a2 = new m1::A();

	m1::m2a::B b1 = new m1::m2a::B();
	m1::m2b::B b2 = new m1::m2b::B();

	m1::m2a::m3a::C c1 = new m1::m2a::m3a::C();
	m1::m2a::m3b::C c2 = new m1::m2a::m3b::C();
	m1::m2b::m3a::C c3 = new m1::m2b::m3a::C();
	m1::m2b::m3b::C c4 = new m1::m2b::m3b::C();

	m1::m2a::m3a::m4::D d1 = new m1::m2a::m3a::m4::D();
	m1::m2a::m3b::m4::D d2 = new m1::m2a::m3b::m4::D();
	m1::m2b::m3a::m4::D d3 = new m1::m2b::m3a::m4::D();
	m1::m2b::m3b::m4::D d4 = new m1::m2b::m3b::m4::D();
	m1::m4alias1::D d5 = new m1::m4alias1::D();
	public Z() {
		System.out.println(this.getClass());
	}
}
