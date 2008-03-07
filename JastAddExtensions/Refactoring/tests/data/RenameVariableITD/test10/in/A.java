// RenameVariableITD/test10/in/A.java p A f g
package p;

aspect X {
    int A.f;
}

class A { }

class B {
    int m(A a) {
	return a.f;
    }
}