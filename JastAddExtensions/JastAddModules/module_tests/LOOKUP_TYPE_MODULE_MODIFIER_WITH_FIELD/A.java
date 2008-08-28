module m1;

public class A {
	m2::org.x.y.B b = new m2::org.x.y.B();
	m2::m3::org.x.y.C c = new m2::m3::org.x.y.C();
	public A() {
		System.out.println(this.getClass());
		System.out.println(m2::org.x.y.B.field);
		System.out.println(m2::m3::org.x.y.C.field);
	}
}
