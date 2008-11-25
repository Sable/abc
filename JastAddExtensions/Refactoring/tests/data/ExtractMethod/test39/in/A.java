class A {
    void m() {
	int[] a = { 23 };
	// from
	a = new int[] { 42 };
	// to
	System.out.println(a[0]);
    }
}