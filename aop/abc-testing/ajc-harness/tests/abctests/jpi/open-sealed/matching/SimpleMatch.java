/*
global jpi void JP() : execution(void *.foo());

class A{
	public static void foo() {}
}

class B {
	public static void foo() {}
}

Sealed class C {
	public static void foo() {}
}

Open class D {
	public static void foo() {}
}

aspect SimpleMatch {
	
	void around JP() {
		System.out.println("hello");
		proceed();
	}
	
	public static void main(String[] args) {
		A.foo();
		B.foo();
		C.foo();
		D.foo();
	}
}*/