// Encapsulate/test11/in/A.java p A i
package p;

class A {
    public int i;
}

class B {
    void m() {
	A a = new A();
	a.i += a.i;
    }
}