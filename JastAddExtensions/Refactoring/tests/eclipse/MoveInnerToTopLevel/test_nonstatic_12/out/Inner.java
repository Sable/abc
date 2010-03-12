package p;
class Inner {
	/** Comment */
	private A a;

	void f(){
		///this.
                a.foo();
	}

	/**
	 * @param a
	 */
	Inner(A a) {
		this.a = a;
	}
}