package Access.test48;

class A {
    class B {
    }
}

public class Test extends A {
    void m() {
	class B { }
        Test.B b;
    }
}
