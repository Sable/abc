class A {
    void m() {
	int i = 23;
	/*[*/n(23)/*]*/;
	System.out.println("back");
    }
    void n(int i) {
	l : while(i == 2) { }
	if(i == 42)
	    return;
	System.out.println("here; i == "+i);
    }
}