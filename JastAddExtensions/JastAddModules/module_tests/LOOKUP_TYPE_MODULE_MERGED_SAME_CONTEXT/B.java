module m2;
public class B {
	m3alias::CType c1 = new m3alias::CType();
	m3c::CType c2 = new m3c::CType();
	m3d::CType c3 = new m3d::CType();

	m3alias::C c4 = new m3alias::C();
	m3c::C c5 = new m3c::C();
	m3d::C c6 = new m3d::C();

	public B() {
		System.out.println(this.getClass());
	}
}
