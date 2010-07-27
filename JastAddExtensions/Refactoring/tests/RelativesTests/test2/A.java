package p;

interface I {
	void m();
}

interface II extends I {
}

class A implements II {
	public void m() { };
}

class B extends A {
	public void m() { };
}

class C extends B implements I {
	public void m() { };
}