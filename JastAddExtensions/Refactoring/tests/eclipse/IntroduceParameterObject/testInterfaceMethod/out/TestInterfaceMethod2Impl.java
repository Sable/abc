package p;

public class TestInterfaceMethod2Impl implements ITestInterfaceMethod {

	public void foo(FooParameter parameterObject) {
		String id = parameterObject.id; ///
		int param = parameterObject.param; ///
		double blubb = parameterObject.blubb; ///
		System.out.println(/*///parameterObject.*/id
				  +/*///parameterObject.*/param
				  +/*///parameterObject.*/blubb);
	}

}
