// RenameMethod/test26/in/A.java p A m() n
package p;

class Z {
    int n() { return 42; }
}

public class A extends Z {
    A a;
    int m() { return 23; }
}

class B {
    A a;
    int p() {
	return a.a.m();
    }
}