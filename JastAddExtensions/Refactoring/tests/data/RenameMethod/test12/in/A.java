// RenameMethod/test12/in/A.java p A m() n
package p;

class Z {
    static void n() { }
}

public class A extends Z {
    static void m() { }
}

class B {
    void m() {
	A a = new A();
	a.n();
    }
}