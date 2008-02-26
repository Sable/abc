// RenameType/test98/in/A.java S T
package p;

public class A {
    <S> int m() { S s; return 23; }
}

class B {
    int x = new A().<B>m();
}