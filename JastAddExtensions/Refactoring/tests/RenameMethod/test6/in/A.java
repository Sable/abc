// RenameMethod/test6/in/A.java p A m() n
package p;

class Z {
    void m() {
    }
    String m(int i) { return "aluis"; }
}

public class A extends Z {
    void m() {
    }
}

class B extends A{
    void m() {
    }
}
