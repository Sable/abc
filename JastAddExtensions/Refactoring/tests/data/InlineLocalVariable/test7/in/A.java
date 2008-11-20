class A {
    void m() {
	int i = 'x';
	n(i);
    }
    void n(int i) {
	System.out.println("here");
    }
    void n(char c) {
	System.out.println("there");
    }
}
