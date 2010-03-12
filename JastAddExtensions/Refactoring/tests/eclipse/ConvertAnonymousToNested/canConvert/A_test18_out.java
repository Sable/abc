package p;
class A{
	private final class Inner extends A {
		void g(){
			int uj= u;
		}
		private Inner(int u) {
			this.u= u;
		}
		private final int u;
	}

	void f(){
		final int u= 9;
		new Inner(u);
	}
}