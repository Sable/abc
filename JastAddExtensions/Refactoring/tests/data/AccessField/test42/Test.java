package Access.test42;

class A {
    int i;
}

class B extends A {
    int i;
}

public class Test {
    B b;
    int m() {
        return ((A)b).i;
    }
}

