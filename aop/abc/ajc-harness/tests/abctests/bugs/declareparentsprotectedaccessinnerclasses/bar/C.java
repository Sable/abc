package bar;

public class C {
    protected static class D {
        public D() {
        }
    }
}
aspect B {
    declare parents: A extends bar.C.D;
}


