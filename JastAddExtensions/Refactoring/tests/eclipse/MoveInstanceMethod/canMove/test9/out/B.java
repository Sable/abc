package p2;

///import p1.A;

public class B {
	public void mB1() {}
	
	public void mB2() {}

	///public void mA1(float j, A a, int foo, String bar) {
	public void mA1(float j, p1.A a, int foo, String bar) {
		this.mB1(); ///mB1();
		a.mA2();
		this.mB2(); ///mB2();
		System.out.println(a);
		System.out.println(bar + j);
	}
}