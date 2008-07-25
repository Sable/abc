module m1;

public class A{
	B b = new B();
	m2::B b2 = new m2::B();
	m2::m3::C c = new m2::m3::C();
	mergedalias::E e = new mergedalias::E();

	public A() {
		System.out.println(this.getClass());
	}
}
