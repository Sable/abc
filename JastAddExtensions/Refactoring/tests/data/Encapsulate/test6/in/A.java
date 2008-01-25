// Encapsulate/test6/in/A.java p A s
package p;

public class A {
    public String s;
}

class B {
    void m() {
	A a = new A();
	a.s += "foo";
    }
}
