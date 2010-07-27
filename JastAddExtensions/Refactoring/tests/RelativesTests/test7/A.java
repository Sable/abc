package p;

interface I {
	void m();
}

class A implements I {
	public void m() { }
}

class B extends A implements Link {
	public void m() { }
}


interface Link {
	public void m();
}

abstract class C {
	abstract public void m();
}

class D extends C implements Link {
	public void m() { }
}