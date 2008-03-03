// RenameVariable/test32/in/A.java p A.B g f
package p;

class A {
    B f;
    class B extends A {
	int g;
	{ B b = B.this.f.f; }
    }
}
