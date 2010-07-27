package p;

interface AI {
	void m();
}

interface AII extends AI {
}

interface AIII extends AII {
	void m();
}

interface BI {
	void n();
}

interface BII extends BI {
}


class A implements AIII {
	public void m() { }
}

class B extends A implements BII {
	public void n() { }
	public void m() { }
	public void n(String s) { }
}