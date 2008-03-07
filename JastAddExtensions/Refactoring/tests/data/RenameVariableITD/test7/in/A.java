// RenameVariableITD/test7/in/A.java p A x a
package p;

aspect X {
    static int a;
    int A.x = a;
}

class A {
    int b;
}
