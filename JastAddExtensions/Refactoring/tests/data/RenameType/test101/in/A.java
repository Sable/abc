// RenameType/test101/in/A.java A B
package p;

public class A { }

class X {
    class Y {
	<T> Y() { }
    }
}

class Z extends X.Y {
    Z() {
	new X().<A> super();
    }
}