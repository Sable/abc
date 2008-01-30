// bug with abc where the declare parents is required to make the
// inner class reference in B be found, but it isn't

class A {
    class Inner {}
}
class B {
    void foo(Inner x) {};
}
public aspect DeclareParentsInner {
    declare parents : B extends A;
}
