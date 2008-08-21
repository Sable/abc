module m2;
package p;

public class P {
	public P() {
		System.out.println(this.getClass());
	}

	public void f() {
		System.out.println("m2::p.P.f()");
	}
}
