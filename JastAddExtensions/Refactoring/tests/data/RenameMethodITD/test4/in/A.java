// RenameMethodITD/test4/in/A.java p A m() n
package p;

aspect A {
    static int m() { return 23; }
    public B.B() {
	this(m());
    }
}

class B {
    public B(int i) { }
}