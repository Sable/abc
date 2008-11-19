class A {
    void m() {
	System.out.println(n() + " " + /*[*/n()/*]*/);
    }
    int x = 42;
    int n() {
	return x++;
    }
}