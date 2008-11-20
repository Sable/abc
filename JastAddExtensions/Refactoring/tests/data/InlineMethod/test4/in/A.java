class A {
    void m() {
	int i;
	i = /*[*/n(23)/*]*/;
    }
    int n(int i) {
	return 42;
    }
}