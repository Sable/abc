package p;

public class ArrayInitializerParameter {
	private String[] test;
	private int[] val;
	public ArrayInitializerParameter(String[] test, int[] val) {
		this.setTest(test); ///this.test = test;
		this.setVal(val);   ///this.val = val;
	}
	//constructor added  
	public ArrayInitializerParameter() {
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