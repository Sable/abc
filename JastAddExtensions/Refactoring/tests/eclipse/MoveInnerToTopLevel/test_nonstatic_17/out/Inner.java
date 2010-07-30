package p;

///import p.A.X;

class Inner {
	/** Comment */
	private A a;

	void f(){
		A.///
                X x= new A.X();
	}

	/**
	 * @param a
	 */
	Inner(A a0) {
		this.a = a0;
	}
}