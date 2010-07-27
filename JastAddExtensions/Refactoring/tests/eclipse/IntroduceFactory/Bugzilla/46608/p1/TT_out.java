package p1;

public class TT {
	// constructor added because the test case wants it private
	private TT() {
		super();
	}
	public static TT createTT() {
		return new TT();
	}
	public void bletch() {
		createTT();
	}
	public void bar() {
		createTT();
	}
}
