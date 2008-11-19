class A {
    void m() {
	int y = 42;
	System.out.println(y++ + " " + /*[*/y/*]*/);
    }
}