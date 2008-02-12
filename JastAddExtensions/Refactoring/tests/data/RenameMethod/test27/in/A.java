// RenameMethod/test27/in/A.java p A m() n
package p;

public class A {
    boolean[] m() { return null; }
}

class B extends A {
    void p() {
	m();
    }
}