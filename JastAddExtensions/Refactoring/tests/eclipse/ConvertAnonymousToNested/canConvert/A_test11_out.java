package p;
class A{
	private final class Inner extends A {
		int k;
		private Inner(int u) {
			this.u= u;
			k= this.///
			   u;
		}
		private final int u;
	}

	void f(){
		final int u= 9;
		new Inner(u);
	}
}