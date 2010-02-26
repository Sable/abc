package p;

public class TestSimpleEnclosingCodeStyle {
	public static class FooParameter {
		public 
		final ///
		String[] strings;
		public int b;
		public FooParameter(String[] strings, int b) {
			this.strings = strings;
			this.b = b;
		}
	}

	public void foo(FooParameter parameterObject){
		final String[] strings = parameterObject.strings;
		int b = parameterObject.b;
		System.out.println(/*///parameterObject.*/strings[0]);
		b++;
	}
	
	public void fooCaller(){
		foo(new FooParameter(new String[]{"Test"}, 6));
	}
}
