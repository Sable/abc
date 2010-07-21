package p;

public class ArrayLengthAccessParameter {
	private int test;
	public ArrayLengthAccessParameter(int test) {
		///this.test = test;
		this.setTest(test);
	}
	//constructor added
	  public ArrayLengthAccessParameter() {
		    super();
		  }
	public int getTest() {
		return test;
	}
	public int /*///void*/ setTest(int test) {
		return ///
		this.test = test;
	}
}