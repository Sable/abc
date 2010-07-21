package p;

public class FieldsWithJavadocData {
	/**
	 * the test field
	 */
	private String[] test;
	/** val field */
	private int[] val;
	// constructors added
	  public FieldsWithJavadocData(String[] test, int[] val) {
	    super();
	    this.setTest(test);
	    this.setVal(val);
	  }
	  public FieldsWithJavadocData() {
	    super();
	  }
	  
	public String[] getTest() {
		return test;
	}
	public String[] /*///void*/ setTest(String[] test) {
		return ///
		this.test = test;
	}
	public int[] getVal() {
		return val;
	}
	public int[] /*///void*/ setVal(int[] val) {
		return ///
		this.val = val;
	}
}