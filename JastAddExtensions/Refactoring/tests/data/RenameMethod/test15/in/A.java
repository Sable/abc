// RenameMethod/test15/in/A.java p A m() n
package p;

public class A {
    void m() {
    }
    class B {
	void n() { }
	void p() {
	    m();
	}
    }
}
