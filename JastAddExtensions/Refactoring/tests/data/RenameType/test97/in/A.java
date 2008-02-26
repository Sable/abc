// RenameType/test97/in/A.java p B C
package p;

public class A {
    <T> int m() { return 23; }
}

class B {
    int x = new A().<B>m();
}