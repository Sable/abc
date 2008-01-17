// Encapsulate/test12/in/A.java p A i
package p;

class A {
    public int i;
    A a[];
    void m() {
        ++(a[++i].i);
    }
}
