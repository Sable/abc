class A {
    int m() {
	// from
	try {
	    return 23;
	} finally {
	    System.out.println(42);
	}
	// to
    }
}
