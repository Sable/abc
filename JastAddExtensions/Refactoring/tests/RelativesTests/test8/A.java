package p;

interface Link12 {
	void m();
}

interface Link23 {
	void m();
}

interface I3 {
	void m();
}


class A1 {
	public void m() { }
}

class B1 extends A1 {
}

class C1 extends B1 implements Link12 {
	public void m() { }
}

class A2 implements Link23 {
	public void m() { }
}

class B2 extends A2 {
}

class C2 extends B2 implements Link12 {
}

class A3 implements I3 {
	public void m() { }
}

class B3 extends A3 {
	public void m() { }
}

class C3 extends B3 implements Link23 {
	public void m() { }
}

