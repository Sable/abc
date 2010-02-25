package p;

public class TestInlineRename {
	int x = foo(new FooParameter(1, 2), 3);
	public static class FooParameter {
		public int xg;
		public int yg;
		public FooParameter(int xg, int yg) {
			this.xg = xg;
			this.yg = yg;
		}
	}
	public int foo(FooParameter parameterObject, int zg) {
		int xg = parameterObject.xg; ///
		int yg = parameterObject.yg; ///
		return /*///parameterObject.*/xg + /*///parameterObject.*/yg;
	}
}

class B extends TestInlineRename {
	public int foo(FooParameter parameterObject, int z) {
		int x = parameterObject.xg; ///
		int y = parameterObject.yg; ///
		System.out.println(/*///parameterObject.*/x);
		foo(new FooParameter(/*///parameterObject.*/x, /*///parameterObject.*/y), z);
		foo(new FooParameter(/*///parameterObject.*/x, z), /*///parameterObject.*/y);
		super.foo(new FooParameter(/*///parameterObject.*/x, z), /*///parameterObject.*/y);
		return super.x;
	}
}
