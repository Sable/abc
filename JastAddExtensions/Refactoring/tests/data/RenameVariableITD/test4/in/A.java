// RenameVariableITD/test4/in/A.java p A b a
package p;

aspect X {
    static int a;
    void B.m() {
	int i = a;
    }
}

class A {
    int b;
}

class B extends A {
}
