module m1;
public class A {
	m2::B b;
	m2::m21::BA ba = new m2::m21::BA();
	m2::m22::BB bb = new m2::m22::BB();
	m2::m23alias::BC bc = new m2::m23alias::BC();
	public A() {
		System.out.println(this.getClass());
	}
}
