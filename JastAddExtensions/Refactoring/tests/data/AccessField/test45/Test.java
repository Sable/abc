package Access.test45;

class A {
    static int i;
}

class B extends A {
    static int i;
}

public class Test {
    int m() {
        return B.i;
    }
}