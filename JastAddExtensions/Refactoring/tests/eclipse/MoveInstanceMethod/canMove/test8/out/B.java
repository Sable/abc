package p2;

///import p1.A;

public class B {
	public String bar= "bar";

	/**
	 * m
	 * @param a TODO
	 * @return Object
	 * @throws Exception
	 */
	///public Object m(A a) throws Exception {
	public Object m(p1.A a) throws Exception {
		System.out.println(a.foo);
		System.out.println(a.foo);
		System.out.println(this.bar); ///System.out.println(bar);
		return null;
	}
}