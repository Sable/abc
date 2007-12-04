package Access.test30;

/*
 *  Test 30: 
 *  access a field in a grandparent interface shadowed by a field of the same name in the
 *  grandchild class
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

interface A {
    float foo = 0;
}

interface B extends A { }

public class Test implements B {
    float foo;
    float m() { return ((A)this).foo; }
}

