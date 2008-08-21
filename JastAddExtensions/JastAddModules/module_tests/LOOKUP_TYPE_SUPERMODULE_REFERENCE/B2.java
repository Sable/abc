module m2x;

public class B extends supermodule::B {
	public B() {
		System.out.println(this.getClass());
	}

	public void f() {
		System.out.println("from m2x");
		super.f();
		System.out.println("m2x::B.f()");
		System.out.println("end from m2x");
	}
}
