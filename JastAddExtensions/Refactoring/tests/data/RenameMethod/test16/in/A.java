// RenameMethod/test16/in/A.java p A m() n
package p;

public class A {
    void m() {
    }
    class B {
	void n() { }
	void p() {
	    A.this.m();
	}
    }
}
