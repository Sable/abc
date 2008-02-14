// RenameVariable/test23/in/A.java p B x y
package p;

class B {
    int x;
}

public class A {
    int y;
    class C extends B {
	int m() { return y; }
    }
}