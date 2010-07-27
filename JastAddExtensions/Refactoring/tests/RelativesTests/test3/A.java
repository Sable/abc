package p;


abstract class A {
	abstract void n();
}

abstract class B extends A {
	abstract void n();
	abstract void m();
	void m(int i) { }
}

abstract class C extends B {
	void n() {	}
}

class D extends C {
	void m() { }
}

abstract class E extends D {
	abstract void n();
}

abstract class F extends E {
	void n(int j) { }
}