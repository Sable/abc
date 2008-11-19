class A {
    void test(int x, int y) {
	while(x < 0) {
	    // from
	    doStuff(--x);
	    y++;
	    // to
	    x = y - 1;
	}
    }
    void doStuff(int x) { }
}
