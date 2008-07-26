module m2;
public class B {
	m3::C c;
	m3::m31::CA ca = new m3::m31::CA();
	m3::m32::CB cb = new m3::m32::CB();
	m3::m33alias::CC cc = new m3::m33alias::CC();
	public B() {
		System.out.println(this.getClass());
	}
}
