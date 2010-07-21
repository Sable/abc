package p;

public class LowestVisibilityParameter {
	private int test2;
	private int test;
	//constructors added
	  public LowestVisibilityParameter(int test2, int test) {
	    super();
	    this.setTest2(test2);
	    this.setTest(test);
	  }
	  public LowestVisibilityParameter() {
	    super();
	  }
	  
	public int getTest2() {
		return test2;
	}
	public int /*///void*/ setTest2(int test2) {
		return ///
		this.test2 = test2;
	}
	public int getTest() {
		return test;
	}
	public int /*///void*/ setTest(int test) {
		return ///
		this.test = test;
	}
}