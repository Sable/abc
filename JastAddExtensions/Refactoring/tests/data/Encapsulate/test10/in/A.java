// Encapsulate/test10/in/A.java p B i
package p;

class A {
    public int getI() { return 23; }
}

class B extends A {
    public int i;
}

class C {
    void m() {
	A a = new B();
	a.getI();
    }
}