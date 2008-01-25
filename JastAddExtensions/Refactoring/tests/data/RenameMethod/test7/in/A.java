// RenameMethod/test7/in/A.java p A m() n
package p;

class Z {
    void m() {
    }
    String n() { return "aluis"; }
}

public class A extends Z {
    void m() {
    }
}

class B extends A{
    void m() {
    }
}
