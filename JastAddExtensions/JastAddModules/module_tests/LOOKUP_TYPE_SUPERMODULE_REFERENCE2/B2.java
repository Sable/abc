module m2xx;

public class B extends supermodule::B {
	public B() {
		System.out.println(this.getClass());
	}

	public void f() {
		System.out.println("from m2xx");
		super.f();
		System.out.println("m2xx::B.f()");
		System.out.println("end from m2xx");
	}
}
