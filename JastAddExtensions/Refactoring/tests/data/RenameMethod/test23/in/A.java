// RenameMethod/test23/in/A.java p A m() n
package p;

class Z {
    static int n() { return 72; }
}

public class A extends Z {
    static int m() { return 23; }
}

class B extends A {
    class C {
	int k = n();
    }
}