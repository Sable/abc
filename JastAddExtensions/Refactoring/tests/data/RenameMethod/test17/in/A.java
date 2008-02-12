// RenameMethod/test17/in/A.java p A m() n
package p;

class Z {
    void n() { }
}

public class A extends Z {
    void m() {
    }
    class B {
	void n() { }
	void p() {
	    A.this.n();
	}
    }
}
