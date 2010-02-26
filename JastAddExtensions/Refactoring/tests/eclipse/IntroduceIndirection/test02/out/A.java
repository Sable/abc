package p;

public class A extends B {
	
	// Test warning because of super keyword

	/**
	 * @param b
	 */
	public static void bar(B b) {
		b.foo();
	}

	{
		///super.foo(); //<------invoke here
		bar(this);
		/* NB: Eclipse does not replace the super call to avoid
		       turning it into a polycall. Our analysis tells us,
		       however, that this doesn't hurt in this case, so
		       we do it anyway. */
	}
}
