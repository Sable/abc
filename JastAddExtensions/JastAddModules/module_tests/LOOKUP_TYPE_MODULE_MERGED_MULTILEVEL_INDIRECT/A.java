module m1;

public class A{

	m2::m4::D d = new m2::m4::D();
	m3::m4::D d2 = new m3::m4::D();
	m4alias::D d3 = new m4alias::D();
	m2::B b = new m2::B();
	m3::C c = new m3::C();

	public A() {
		System.out.println(this.getClass());
	}
}
