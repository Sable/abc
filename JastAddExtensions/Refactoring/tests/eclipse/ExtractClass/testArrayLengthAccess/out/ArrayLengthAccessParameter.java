package p;

public class ArrayLengthAccessParameter {
	private int test;
	public ArrayLengthAccessParameter(int test) {
		///this.test = test;
		this.setTest(test);
	}
	public int getTest() {
		return test;
	}
	public int /*///void*/ setTest(int test) {
		return ///
		this.test = test;
	}
}