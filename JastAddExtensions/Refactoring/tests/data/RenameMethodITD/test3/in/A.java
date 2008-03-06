// RenameMethodITD/test3/in/A.java p A m() n
package p;

aspect X {
    static int n() { return 23; }
    int A.x = n();
}

public class A {
    int m() { return 42; }
    { System.out.println(x); }
}