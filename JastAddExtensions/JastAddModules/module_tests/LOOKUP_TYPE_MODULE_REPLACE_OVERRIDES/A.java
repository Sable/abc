module m1;
public class A {
	m2::b.B b1 = new m2::b.B();
	msynthetic::b.B b2 = new msynthetic::b.B();
	m3::b.B b3 = new m3::b.B();
	m2other::b.B b4 = new m2other::b.B();

	public A() {
		System.out.println(this.getClass());
	}
}
