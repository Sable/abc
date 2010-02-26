package p;

public class VariableDeclarationInitializerParameter {
	private int test;
	public VariableDeclarationInitializerParameter(int test) {
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