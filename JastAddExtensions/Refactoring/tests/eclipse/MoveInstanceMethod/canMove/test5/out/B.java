package p2;

///import p1.A;

public class B {

	///public void mA1(A a) {
	public void mA1(p1.A a) {
		System.out.println(p1.A.fgHello); ///System.out.println(A.fgHello);
		a.talk(this); ///A.talk(this);
		System.out.println(a);
	}
}