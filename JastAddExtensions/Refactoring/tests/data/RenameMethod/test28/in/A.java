// RenameMethod/test28/in/A.java p A m(int) n
package p;

class Z {
    void n() { }
}

public class A {
    boolean[] m(int i) { return null; }
    class B extends Z {
	void p() {
	    m(42);
	}
    }
}
