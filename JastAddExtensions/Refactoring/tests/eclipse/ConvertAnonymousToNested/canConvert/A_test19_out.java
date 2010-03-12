package p;
class A{
	private final class Inner extends A {
		int l; ///= 9;
		int p0 /*///= 2*/, k, k1;
		int l1 /*///=l+1*/, p, q;
		private Inner(int u) {
			this.u= u;
			l = 9; ///
			p0 = 2; ///
			k= this.///
			   u;
			k1= k;
			l1 = l+1; ///
			q= p+ this.///
			      u;
		}
		private final int u;
	}

	void f(){
		final int u= 8;
		new Inner(u);
	}
}