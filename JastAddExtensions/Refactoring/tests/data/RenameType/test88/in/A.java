// RenameType/test88/in/A.java p D.B C
package p;

class D {
    static class B {
	static int x = 42;
    }
}

public class A {
    static class C extends D {
	static int x = 23;
	static int m() { return C.x; }
    }
    public static void main(String[] args) {
	System.out.println(C.m());
    }
}