// RenameVariableITD/test1/in/A.java p A.B b a
package p;

class A {
    int a;
    class B {
	void v() {
	    int x = a;
	}
    }
}

aspect X {
    int A.B.b;
}
