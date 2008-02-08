package Access.test27;

/*
 *  Test 27: 
 *  access a field in the grandparent class shadowed by a field of the same name in the
 *  grandchild class
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

class A {
    float foo;
}

class B extends A { }

public class Test extends B {
    float foo;
    float m() { return super.foo; }
}

