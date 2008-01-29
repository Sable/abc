package Access.test41;

class A {
    static class B {
        static int i;
        class C {
            int B;
            int m() { return A.B.i; }
        }
    }
}
