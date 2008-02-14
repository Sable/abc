// RenameType/test79/in/A.java p B.C D
package p;

class B {
    class C { }
}

class A {
    class D { }
    class E extends B {
	D d;
    }
}