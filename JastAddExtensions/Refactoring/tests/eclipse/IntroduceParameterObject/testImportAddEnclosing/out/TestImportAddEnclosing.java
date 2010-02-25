package p;

import java.security.Permission;

public class TestImportAddEnclosing {
	public static class FooParameter {
		public Permission[] a;
		public int b;
		public FooParameter(Permission[] a, int b) {
			this.a = a;
			this.b = b;
		}
	}

	public void foo(FooParameter parameterObject){
		Permission[] a = parameterObject.a; ///
		int b = parameterObject.b;          ///
	}
}
