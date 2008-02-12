// RenameMethod/test24/in/A.java p A m() n
package p;

public class A {
    int m() { return 23; }
}

class B {
    A a;
    int p() {
	return a.m();
    }
}