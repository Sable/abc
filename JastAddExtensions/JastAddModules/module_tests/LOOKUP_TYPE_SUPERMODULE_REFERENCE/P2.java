module m2x;
package p;

public class P extends supermodule::p.P {
	public P() {
		System.out.println(this.getClass());
	}

	public void f() {
		System.out.println("from m2x");
		super.f();
		System.out.println("m2x::p.P.f()");
		System.out.println("end from m2x");
	}
}
