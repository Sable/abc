class A {
    void m() {
	/*[*/n()/*]*/;
	System.out.println("world!");
    }
    void n() {
	System.out.println("Hello, ");
    }
}

class B extends A {
    void n() {
	System.out.println("Howdy, ");
    }
}