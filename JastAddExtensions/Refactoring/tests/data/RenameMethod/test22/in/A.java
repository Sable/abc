// RenameMethod/test22/in/A.java p A m() n
package p;

public class A {
    static int m() { return 23; }
    static class B {
	static void n(int i) { }
	int k = m();
    }
}
