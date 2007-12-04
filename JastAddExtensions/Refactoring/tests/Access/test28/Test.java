package Access.test28;

/*
 *  Test 28: 
 *  access a field in the grandparent class shadowed by a field of the same name in the
 *  parent class
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

class A {
    float foo;
}

class B extends A { 
    float foo;
}

public class Test extends B {
    float m() { return ((A)this).foo; }
}

