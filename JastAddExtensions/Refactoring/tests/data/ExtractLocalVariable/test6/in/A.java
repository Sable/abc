class A {
    void m() {
	int y = 23;
	System.out.println((y=42) + "" + /*[*/(y=56)/*]*/);
	System.out.println(y);
    }
}