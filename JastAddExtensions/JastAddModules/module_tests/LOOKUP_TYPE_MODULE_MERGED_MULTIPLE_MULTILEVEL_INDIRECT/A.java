module m1;
public class A{
	m2a::B b1 = new m2a::B();
	m2b::B b2 = new m2b::B();
	m2a::m3::C c1 = new m2a::m3::C();
	m2b::m3::C c2 = new m2b::m3::C();
	m5merged1::E e1 = new m5merged1::E();
	m2a::m3::m4a::m5::E e2 = new m2a::m3::m4a::m5::E();
	m2a::m3::m4b::m5::E e3 = new m2a::m3::m4b::m5::E();
	m2b::m3::m4a::m5::E e4 = new m2b::m3::m4a::m5::E();
	m2b::m3::m4b::m5::E e5 = new m2b::m3::m4b::m5.E();

	public A() {
		System.out.println(this.getClass());
	}
}
