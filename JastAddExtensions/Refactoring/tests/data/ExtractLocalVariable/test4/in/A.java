class A {
    int y = 42;
    void m() {
	System.out.println(y++ + " " + /*[*/y/*]*/);
    }
}