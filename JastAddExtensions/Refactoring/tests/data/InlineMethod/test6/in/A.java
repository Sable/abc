class A {
    void m() {
	int i = 23;
	/*[*/n(i++)/*]*/;
    }
    void n(int j) {
	if(j == 23)
	    System.out.println("magic number!");
	else
	    System.out.println("something else: "+j);
    }
}