// RenameVariableITD/test2/in/A.java p A b a
package p;

aspect X {
    static int a;
    void A.m() {
	int i = a;
    }
}

class A {
    int b;
}
