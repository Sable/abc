class A {
	int x;
	class B { int x; }
        B b = new B();
}
aspect Aspect {
	static int x;
	static int y;
	int A.B.foo() {
		class C {
			int x = 3;
			int bar() {return x + A.this.x;}
		}
		return this.x + (new C()).bar() + y;
	}
}

public class QualThis {

    public static void main(String[] args) throws Exception {
	A a = new A();
	if(3 != a.b.foo()) throw new Exception("Qualified this didn't work - expecting 3, got " + a.b.foo());
    }
}
