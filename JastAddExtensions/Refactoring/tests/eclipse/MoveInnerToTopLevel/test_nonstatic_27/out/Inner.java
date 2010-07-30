package p;
class Inner {
	/** Comment */
	private A a;

	void f(){
		a///A
		.foo();
	}

	/**
	 * @param a
	 */
	Inner(A a0) {
		this.a = a0;
	}
}