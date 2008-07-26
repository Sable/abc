module m2;
public class B{
	m1::A a;
	m1::m3::C c = new m1::m3::C();
	m4::D d = new m4::D();

	public B() {
		System.out.println(this.getClass());
	}
}
