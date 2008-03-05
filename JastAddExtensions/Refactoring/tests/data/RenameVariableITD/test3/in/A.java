// RenameVariableITD/test3/in/A.java b a
package p;

aspect X {
    static int a;
    void A.m(int b) {
	int i = a;
    }
}

class A {
}
