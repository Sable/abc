class A {
    void m() {
	int x = 23;
	/*[*/n()/*]*/;
    }
    int x = 42;
    void n() {
	System.out.println(x);
    }
}