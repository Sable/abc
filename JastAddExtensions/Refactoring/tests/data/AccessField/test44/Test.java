package Access.test44;

class A {
    int i;
}

class B extends A {
    B b;
    int i;
}

public class Test {
    B b;
    int m() {
        return b.b.i;
    }
}

