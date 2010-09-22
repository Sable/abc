package p;
class A {

	private final class Inner1Extension extends Inner1 {
		public void m1(String s2) {
			String s3 = param + s2;
		}
		private Inner1Extension(String param0, int param) {
			super(param);
			this.param= param0;
		}
		private final String param;
	}

	class Inner1 {
		public Inner1(int param) {			
		}		
	}
	
	public void doit(final int a, final String param) {
		Object o = new Inner1Extension(param, a);
	}
}
