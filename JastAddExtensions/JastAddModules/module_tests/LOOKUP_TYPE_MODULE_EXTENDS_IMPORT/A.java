module m1;

import m2x::B;
import m2x::BX;
import m2x::p.*;
import m2x::q.Q;

public class A {
	//m2x::B b = new m2x::B();
	//m2x::BX bx = new m2x::BX();
	//m2x::p.P p = new m2x::p.P();
	//m2x::q.Q q = new m2x::q.Q();

	B b = new B();
	BX bx = new BX();
	P p = new P();
	Q q = new Q();
	public A() {
		System.out.println(this.getClass());
	}
}
