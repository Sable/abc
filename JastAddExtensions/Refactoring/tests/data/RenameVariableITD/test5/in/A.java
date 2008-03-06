// RenameVariableITD/test5/in/A.java p A b a
package p;

aspect X {
    int a;
    void A.m() {
	int i = a;
    }
}

class A {
    int b;
}
