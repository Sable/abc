class SupA { int x; }
class SupB { int x; }
class SupC { int x = 3; }

class A extends SupA {
        int x = 42;
	class B extends SupB { int x = 42; }
        B b = new B();
}
aspect Aspect {
	static int x;
	static int y;
	int A.B.foo() {
		class C extends SupC {
		        int x = 42;
			int bar() {return super.x + A.super.x;}
		}
		return super.x + (new C()).bar() + y;
	}
}

public class QualSupers {

    public static void main(String[] args) throws Exception {
	A a = new A();
	if(3 != a.b.foo()) throw new Exception("Qualified supers didn't work - expecting 3, got " + a.b.foo());
    }
}
