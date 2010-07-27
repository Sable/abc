package p;

interface Link {
	void m();
}

class A1 {
	public void m() { }
}

class B1 extends A1 { }

class C1 extends B1 implements Link { }


class A2 {
	public void m() { }
}

class B2 extends A2 { }

class C2 extends B2 implements Link { }