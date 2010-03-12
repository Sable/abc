package p;
class Inner {
	/** Comment */
	private A a;

	void f(){
		new Inner(///this.
                          a);
	}

	/**
	 * @param a
	 */
	Inner(A a) {
		this.a = a;
	}
}