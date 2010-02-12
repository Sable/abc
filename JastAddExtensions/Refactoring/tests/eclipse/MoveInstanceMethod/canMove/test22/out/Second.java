package p;
class Second {
	public void foo(Second s) {
		s.bar();
	}

	public void bar() {
	}
	
	public void go(int i, int j) {
	}

	public void print() {
		this.foo(this); ///foo(this);
		this.bar();     ///bar();
		this.go(17, 18); ///go(17, 18);
	}
}
