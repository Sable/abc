package p;

interface I {
	void m();
}

abstract class B implements I { }

class C extends B {
	public void m() { }
}

class D implements I {
	public void m() { }
}

