package p;

public class TestSubclassInCU {
	int x = foo(new FooParameter(1, 2, 3));

	public int foo(FooParameter parameterObject) {
		int xg = parameterObject.xg; ///
		int yg = parameterObject.yg; ///
		int zg = parameterObject.zg; ///
		return xg + yg; ///return parameterObject.xg + parameterObject.yg;
	}
}

class B extends TestSubclassInCU {
	public int foo(FooParameter parameterObject) {
		///System.out.println(parameterObject.xg);
		///foo(new FooParameter(parameterObject.xg, parameterObject.yg, parameterObject.zg));
		///this.foo(new FooParameter(parameterObject.xg, parameterObject.zg, parameterObject.yg));
		///new B().foo(new FooParameter(parameterObject.xg, parameterObject.zg, parameterObject.yg));
		///super.foo(new FooParameter(parameterObject.xg, parameterObject.zg, parameterObject.yg));
		///return super.x;
		int x = parameterObject.xg;
		int y = parameterObject.yg;
		int z = parameterObject.zg;
		System.out.println(x);
		foo(new FooParameter(x, y, z));
		this.foo(new FooParameter(x, z, y));
		new B().foo(new FooParameter(x, z, y));
		super.foo(new FooParameter(x, z, y));
		return super.x;
	}
}