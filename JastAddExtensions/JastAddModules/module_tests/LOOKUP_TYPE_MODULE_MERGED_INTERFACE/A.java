module m1;

import m2merged::B;

public class A {
	B b = new B();
	public A() {
		System.out.println(this.getClass());
	}
}
