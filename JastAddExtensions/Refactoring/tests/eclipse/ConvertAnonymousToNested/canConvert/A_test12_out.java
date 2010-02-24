package p;
class A{
	private final class Inner extends A {
		private final int u;
		int k;
		private Inner(int u, int x) {
			super(x);
			this.u= u;
			k= this.///
			   u;
		}
	}
	A(int x){
	}
	void f(){
		final int u= 9;
		int s= 2;
		new Inner(u, s);
	}
}