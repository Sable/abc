// RenameField/test14/in/A.java p A.C c b
package p;
public class A {
    class B { int b; }
    class C extends B { int c; }
    class D { C c; int m() { return c.b; } }
}