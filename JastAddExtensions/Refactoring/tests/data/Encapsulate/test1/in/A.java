// Encapsulate/test1/in/A.java p A i
package p;

class A {
    int i;
}

class B extends A {
    int getI() { return 42; }
}

class C {
    void m() {
	A a = new B();
	a.i = 23;
    }
}