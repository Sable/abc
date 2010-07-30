package p;
public class Inner {

	/** Comment */
	private final A a;

	public Inner(A a0) {
		super();
		this.a = a0;
		System.out.println(
		    this.///
		    getName());
	}

	public String getName() {
		return ///this.
		       a.getTopName() + ".Inner";
	}
}