package test;

/*
 * Tests:
 *   - rename A.i into A.z: no further changes needed
 *   - rename A.i into A.j: renaming impossible due to name clash
 *   - rename B.l into B.i: no further changes needed
 *   - rename B.l into B.j: need to adjust reference to j in assignment to l
 *   - rename B.l into B.k: need to adjust reference to k in assignment to n in class C
 *   - rename B.l into B.p: need to adjust reference to l in method B.bar
 *   - rename B.l into B.q: need to adjust reference in B.bar
 *   - rename B.l into B.r: need to adjust reference in B.bar
 *   - rename B.l into B.s: need to adjust references in B.bar and C.bluis
 *   - rename B.l into B.t: need to adjust references in B.bar
 *   - A.k cannot be renamed into A.l, since this would lead to an error
 *     on the reference 'l.i'
 */

public class TestSrc {
    String s;

    static class i { static i tst; }

    static class A {
        int i;
        static int j;
        static class l { static int i = 1; };
        class f { int g = A.this.i; };
        int k;
        l tst;
        void tst() {
            int z = /*TestSrc.A.*/l.i;
        }
    }

    class Z extends A {
        int A;
        int i = test.TestSrc.A.j;
    }

    class B extends A {
        int l;
        int m = j;
        void foo() {
            int p;
            p = 23;
        }
        void bar() {
            int q;
            l = 42;
        }
        class g { int f = B.super.j; };
        Object o;
        void baz() {
            int l;
            final int u = 476;
            o = new Object() {
                int v;
                void tst() {
                    int w = u;
                }
            };
            l = 117;
        }
    }

    class C extends B {
        int n = k;
        class aa extends C { int bb = ((A)C.this).i; int cc = n; 
        class dd { int ee = ((A)C.this).i; int ff = i; } }
        void aluis() {
            int r;
            r = 56;
        }
        void bluis() {
            int s;
            l = 24;
        }
    }

    class D extends B {
        int t;
        void cluis() {
            l = 77;
        }
    }

}
