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

public class Sep9 {

    public static void main(String[] args) {
	A a = new A();
	System.out.println(a.b.foo());
    }
}