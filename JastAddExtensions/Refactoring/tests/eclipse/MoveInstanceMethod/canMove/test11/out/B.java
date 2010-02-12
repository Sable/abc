package p2;

///import p1.A;

public class B {
	public void mB1() {}
	
	public void mB2() {}

	/**
	 * This is a comment
	 * @param a TODO
	 * @param j
	 * @param foo
	 * @param bar
	 */
	///public void mA1(A a, float j, int foo, String bar) {
	public void mA1(p1.A a, float j, int foo, String bar) {
		this.mB1(); ///mB1();
		System.out.println(bar + j + a);
		String z= a.fString + a.fBool;
		a.fInt++;
	}
}