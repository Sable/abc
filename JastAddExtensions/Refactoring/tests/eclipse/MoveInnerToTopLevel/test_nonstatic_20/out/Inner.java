package p;
class Inner{
	/** Comment */
	private A a;
	Inner(A a0){
		super();
		this.a = a0;
	}
	Inner(A a, int i){
		this(a);
	}
	Inner(A a, boolean b){
		this(a, 1);
	}
}