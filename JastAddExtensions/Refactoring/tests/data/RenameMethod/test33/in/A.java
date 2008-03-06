// RenameMethod/test33/in/A.java p A m() n
package p;

public class A {
    public void m() { System.out.println(42); }
}

class B {
    static void n() { System.out.println(23); }
    class C extends A {
	{ n(); }
    }
}
