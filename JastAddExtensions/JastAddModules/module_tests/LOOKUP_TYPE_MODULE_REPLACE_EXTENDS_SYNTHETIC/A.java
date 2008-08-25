module m1;

import m2::m3int::C;

public class A {
	B b = new B();
	C c = new C();
	public A() {
		System.out.println(this.getClass());
	}
}
