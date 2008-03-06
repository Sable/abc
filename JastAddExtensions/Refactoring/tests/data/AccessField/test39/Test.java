package Access.test39;

/*
 *  Test 39: 
 *  from a static context, access a field in the superclass hidden
 *  by a field of the same name
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

class A {
    static float foo;
}

public class Test extends A {
    static float foo;
    static { foo = 1.0f; }
}

