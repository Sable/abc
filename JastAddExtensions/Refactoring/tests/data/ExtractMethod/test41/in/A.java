class A {
    void m() {
	boolean b = false;
	int i = 23;
	// from
	if(b) i = 42;
	// to
	System.out.println(i);
    }
}