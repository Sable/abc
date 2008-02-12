// RenameMethod/test18/in/A.java p A m() n
package p;

public class A {
    void m() {
    }
}


class B extends A {
    void n() { }
    class C {
	void p() {
	    m();
	}
    }
}