package test;

public class TestSrc2 {

	class A { int i; }
	
	class B extends A { int i; } 
	
	class C { 
		int j;
		B b;
		int k = b.i;
		Object o = new Object() {
			int j;
			int m() {
				return C.this.j;
			}
		};
	}
	
	class D extends A {
		int TestSrc2;
	}
	
	void m() {
		B b = new B();
		b.i = 42;
	}
	
}

class X {
	static void m() {}
	void n() {}
}

class Y extends X {
	static void m() { }
	void n() {}
}

class Z {
	X x = new Y();
	{ x.m(); x.n(); }
}