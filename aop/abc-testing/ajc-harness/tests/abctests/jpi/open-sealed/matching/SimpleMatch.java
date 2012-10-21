import org.aspectj.testing.Tester;

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
	
	public static int executionCounter = 0;
	
	void around JP() {
		SimpleMatch.executionCounter++;
		proceed();
	}
	
	public static void main(String[] args) {
		A.foo();
		B.foo();
		C.foo();
		D.foo();
		Tester.checkEqual(SimpleMatch.executionCounter,1, "expected 1 matches but saw "+SimpleMatch.executionCounter);
	}
}