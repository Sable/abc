
public class TestDefaultPackagePointTopLevel {
	public static void main(String[] args) {
		new TestDefaultPackagePointTopLevel().foo(new ArrayList(5, 6));
	}
	public void foo(ArrayList parameterObject){
		int xer = parameterObject.xer;
		int yer = parameterObject.yer;
		System.out.println(xer+yer);
	}
}
