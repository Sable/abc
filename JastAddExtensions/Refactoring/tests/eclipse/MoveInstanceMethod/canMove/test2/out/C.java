package p3;

import p1.A;
import p2.B;

class C {
	C() {
		A a= new A();
		B b = new B(); b.mA1(a); ///new B().mA1(a);
	}	
}