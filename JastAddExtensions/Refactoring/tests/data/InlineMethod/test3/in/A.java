class A {
    void m() {
	String msg = "Hello, ";
	/*[*/n(msg)/*]*/;
	System.out.println("world!");
    }
    void n(String msg) {
	System.out.println(msg);
    }
}