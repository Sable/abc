package Access.test31;

/*
 *  Test 31: 
 *  access a field in a grandparent interface shadowed by a field of the same name in a
 *  parent interface
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

interface A {
    float foo = 0;
}

interface B extends A { 
    float foo = 1;
}

public class Test implements B {
    float m() { return ((A)this).foo; }
}

