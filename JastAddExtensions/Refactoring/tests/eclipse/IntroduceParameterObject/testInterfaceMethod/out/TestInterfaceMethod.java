package p;

public class TestInterfaceMethod implements ITestInterfaceMethod {
	/* (non-Javadoc)
	 * @see p.ITestInterfaceMethod#foo(java.lang.String, int, double)
	 */
	public void foo(FooParameter parameterObject){
		String id = parameterObject.id; ///
		int param = parameterObject.param; ///
		double blubb = parameterObject.blubb; ///
		foo(new FooParameter(id, param, blubb)); ///foo(parameterObject);
	}
}
