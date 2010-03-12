package p;
class Inner {
	/** Comment */
	private A a;

	void f(){
		A.i= 1;
	}

	/**
	 * @param a
	 */
	Inner(A a) {
		this.a = a;
	}
}