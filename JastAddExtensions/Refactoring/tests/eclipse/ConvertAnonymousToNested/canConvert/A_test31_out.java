package p;
public class A {

	private final class Inner1Extension extends Inner1 {
		private final String param;
		private Inner1Extension(String param0, int param) {
			super(param);
			this.param= param0;
		}
		public void m1(String s2) {
			String s3 = param + s2;
		}
	}

	class Inner1 {
		public Inner1(int param) {			
		}		
	}
	
	public void doit(final int a, final String param) {
		Object o = new Inner1Extension(param, a);
	}
}
