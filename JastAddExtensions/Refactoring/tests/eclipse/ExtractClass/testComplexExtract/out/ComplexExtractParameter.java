package p;

public class ComplexExtractParameter {
	public int test;
	public int test2;
	public int test3;
	public int test4;
	/*public ComplexExtractParameter(int test2, int test4) {
		this.test2 = test2;
		this.test4 = test4;
	}*/

	// contructors added
	  public ComplexExtractParameter(int test, int test2, int test3, int test4) {
		    super();
		    this.test = test;
		    this.test2 = test2;
		    this.test3 = test3;
		    this.test4 = test4;
		  }
		  public ComplexExtractParameter() {
		    super();
		  }
}