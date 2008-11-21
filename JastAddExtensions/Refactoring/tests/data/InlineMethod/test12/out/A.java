class A {
    void m() {
	int j;
	int i = 23;
	j = n(i);
	System.out.println("back");
    }
    int n(int i) {
	System.out.println("here");
	return i = 42;
    }
}