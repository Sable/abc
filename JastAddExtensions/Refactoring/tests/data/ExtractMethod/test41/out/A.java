class A {
    void m() {
	boolean b = false;
	int i = 23;
	i = extracted(i);
	System.out.println(i);
    }
    protected int extracted(boolean b, int i) {
	if(b)
	    i = 42;
	return i;
    }
}