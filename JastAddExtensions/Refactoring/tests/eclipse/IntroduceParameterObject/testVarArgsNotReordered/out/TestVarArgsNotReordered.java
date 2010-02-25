package p;

public class TestVarArgsNotReordered {
	public static class FooParameter {
		public String[] a;
		public int[] is;
		public FooParameter(String[] a, int[] is) { ///int... is) {
			this.a = a;
			this.is = is;
		}
	}

	public void foo(FooParameter parameterObject) {
	    String[] a = parameterObject.a; ///
	    int[] is = parameterObject.is; ///
	}

	public void fooCaller() {
		/*///foo(new FooParameter(new String[0]));
		foo(new FooParameter(new String[0], null));
		foo(new FooParameter(new String[0], new int[] {1,2,3,4}));
		foo(new FooParameter(new String[0], 1, 2, 3, 4, 5));
		foo(new FooParameter(new String[0], Integer.parseInt("5")));
		foo(new FooParameter(new String[0], new Integer(6).intValue(), 2, 3));*/
		foo(new FooParameter(new String[0], new int[]{}));
		foo(new FooParameter(new String[0], null));
		foo(new FooParameter(new String[0], new int[] {1,2,3,4}));
		foo(new FooParameter(new String[0], new int[] {1, 2, 3, 4, 5}));
		foo(new FooParameter(new String[0], new int[] {Integer.parseInt("5")}));
		foo(new FooParameter(new String[0], new int[] {new Integer(6).intValue(), 2, 3}));
	}
}
