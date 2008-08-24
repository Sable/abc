module m1;
public class A {
	m2a::B b = new m2a::B();
	m2b::B b2 = new m2b::B();
	m2merged::B b3 = new m2merged::B();

	m4::D d = new m4::D();
	m2merged::m4::D d2 = new m2merged::m4::D();
	m2new2::m4::D d3 = new m2new2::m4::D();
	m2merged::m3::m4::D d4 = new m2merged::m3::m4::D();
	m2new::m3::m4::D d5 = new m2new::m3::m4::D();

	public A() {
		System.out.println(this.getClass());
	}
}
