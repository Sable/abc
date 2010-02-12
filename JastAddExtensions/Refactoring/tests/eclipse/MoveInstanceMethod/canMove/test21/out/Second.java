package p;
class Second {
	public void foo(Second s) {
		s.bar();
	}

	public void bar() {
	}
	
	public void go(int i, int j) {
	}

	public void print(A a) {
		this.foo(this);  ///foo(this);
		this.bar();      ///bar();
		this.go(17, 18); ///go(17, 18);
	
		a.equals(a);
		this.foo(a.s2);  ///foo(a.s2);
		a.s2.bar();
	}
}

