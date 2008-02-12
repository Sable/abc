// RenameMethod/test20/in/A.java p A m() n
package p;

class Z {
    static void n() { }
}

public class A extends Z {
    static void m() { }
}

class B extends A {
    { n(); }
}