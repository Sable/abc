package p3;

import p1.A;
import p2.B;

class C {
    C() {
	B b = getB(); b.mA1(getA()); /// getB().mA1(getA());
	}

	A getA() {
		return null;
	}

	B getB() {
		return null;
	}
}