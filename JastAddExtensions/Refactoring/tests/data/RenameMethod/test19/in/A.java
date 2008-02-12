// RenameMethod/test19/in/A.java p A m() n
package p;

class Z {
    static void n() { }
}

public class A extends Z {
    static void m() { }
    static {
	n();
    }
}
