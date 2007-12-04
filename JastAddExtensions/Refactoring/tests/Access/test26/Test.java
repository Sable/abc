package Access.test26;

/*
 *  Test 26: 
 *  access a field in the superclass shadowed by a field of the same name
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

class A {
    float foo;
}

public class Test extends A {
    float foo;
    { super.foo = 1.0f; }
}

