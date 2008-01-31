// RenameType/test62/in/A.java p A.B C
package p;

class A {
    class B { }
}

class C {
    static class D extends A {
	int D;
	static int m() { return 23; }
	int i = C.D.m();
    }
}