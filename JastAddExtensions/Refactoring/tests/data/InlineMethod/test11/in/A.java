class A {
    void m() {
	int j;
	j = /*[*/n(23)/*]*/;
	System.out.println("back");
    }
    int n(int i) {
	System.out.println("here");
	return i = 42;
    }
}