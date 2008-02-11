package Access.test43;

class A {
    int i;
}

class B extends A {
    int i;
}

public class Test {
    B b;
    int m() {
        return b.i;
    }
}

