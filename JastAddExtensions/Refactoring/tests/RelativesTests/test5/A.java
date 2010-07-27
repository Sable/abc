package p;


interface AI {
	void m();
}

interface AII extends AI {
}

interface BI {
	void m();
}

abstract class A implements AII {
	public void m(int j) { }
}

class B extends A implements BI {
	public void m() { }
}