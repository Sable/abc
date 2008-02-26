// RenameType/test100/in/A.java A B
package p;

public class A {
    class C { 
	public <T> C() { }
    }
}

class D {
    Object o = new A().new<A> C();
}