// RenameMethodITD/test2/in/A.java p A m() n
package p;

public aspect A {
    static void m() { }
    void X.r() { m(); }
}

class X {
    void n() { }
}