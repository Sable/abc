package p;

public class ComplexExtractGetterSetterParameter {
	private int test;
	private int test2;
	private int test3;
	private int test4;
	/*public ComplexExtractGetterSetterParameter(int test2, int test4) {
		this.setTest2(test2); ///this.test2 = test2;
		this.setTest4(test4); ///this.test4 = test4;
	}*/
	// contructors added
	public ComplexExtractGetterSetterParameter(int test, int test2, int test3, int test4) {
	    super();
	    this.setTest(test);
	    this.setTest2(test2);
	    this.setTest3(test3);
	    this.setTest4(test4);
	  }
	  public ComplexExtractGetterSetterParameter() {
	    super();
	  }
	public int getTest() {
		return test;
	}
	public int ///void 
	       setTest(int test) {
		return ///
		this.test = test;
	}
	public int getTest2() {
		return test2;
	}
	public int ///void 
	       setTest2(int test2) {
		return ///
		this.test2 = test2;
	}
	public int getTest3() {
		return test3;
	}
	public int ///void 
	       setTest3(int test3) {
		return ///
		this.test3 = test3;
	}
	public int getTest4() {
		return test4;
	}
	public int ///void 
	       setTest4(int test4) {
		return ///
		this.test4 = test4;
	}
}