package Access.test46;

class A {
    int B;
    static class B {
        static int i;
        class C {
            int B;
            int m() { return A.B.i; }
        }
    }
}
