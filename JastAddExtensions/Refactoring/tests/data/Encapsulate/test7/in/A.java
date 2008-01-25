// Encapsulate/test7/in/A.java p A i
package p;

public class A {
    protected int i;
}

class B extends A { }

class C {
    void m() {
	B b = new B();
	++b.i;
    }
}
