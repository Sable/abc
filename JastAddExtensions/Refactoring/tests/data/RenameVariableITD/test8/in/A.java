// RenameVariableITD/test8/in/A.java p A x b
package p;

aspect X {
    static int a;
    int A.x = a;
}

class A {
    int b;
}
