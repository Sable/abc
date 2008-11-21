class A {
    void m() {
	int i = 23;
	n: {
	    if(i == 42)
		break n;
	    System.out.println("here; i == "+i);
	}
	System.out.println("back");
    }
    void n(int i) {
	if(i == 42)
	    return;
	System.out.println("here; i == "+i);
    }
}