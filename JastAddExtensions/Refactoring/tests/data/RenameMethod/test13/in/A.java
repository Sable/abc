// RenameMethod/test13/in/A.java p A m() n
package p;

class Z {
    void n() { }
}

public class A extends Z {
    void m() { }
}

class B {
    void m() {
	A a = new A();
	a.n();
    }
}