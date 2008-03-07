// RenameVariableITD/test9/in/A.java p A b a
package p;

aspect X {
    int A.m() {
	int a;
	return b;
    }
}

class A {
    int b;
}
