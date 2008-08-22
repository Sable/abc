module m2xx;
package p;

public class P extends supermodule::p.P {
	public P() {
		System.out.println(this.getClass());
	}

	public void f() {
		System.out.println("from m2xx");
		super.f();
		System.out.println("m2xx::p.P.f()");
		System.out.println("end from m2xx");
	}
}
