class A {
	int x;
	void m() {
		// from
		x = 1;  
		int x = 2;
		// to
		System.out.println(x);
	}
}
