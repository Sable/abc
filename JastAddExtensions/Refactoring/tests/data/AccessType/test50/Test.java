package Access.test50;

class A {
    class B {
    }
}

public class Test extends A {
    class B { }
    class C extends A {
        Test.B b;
    }
}
