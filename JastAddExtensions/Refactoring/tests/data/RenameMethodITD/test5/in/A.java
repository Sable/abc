// RenameMethodITD/test5/in/A.java p A m() n
package p;

aspect A {
    static void m() { }
    public B.B() {
	m();
    }
}

class B {
}