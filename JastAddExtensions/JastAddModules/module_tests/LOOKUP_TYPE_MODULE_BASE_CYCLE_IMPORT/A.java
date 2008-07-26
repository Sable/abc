module m1;
public class A{
	m2::B b;
	m2::m4::D d = new m2::m4::D();
	m3::C c = new m3::C();
	public A() {
		System.out.println(this.getClass());
	}
}
