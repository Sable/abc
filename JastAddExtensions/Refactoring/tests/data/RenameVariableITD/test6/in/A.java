// RenameVariableITD/test6/in/A.java p A b a
package p;

aspect X {
    static int a;
    int A.x = a;
}

class A {
    int b;
}
