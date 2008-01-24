package Access.test29;

/*
 *  Test 29: 
 *  access a field in a superinterface shadowed by a field of the same name
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

interface A {
    float foo = 0;
}

public class Test implements A {
    float foo;
    { foo = ((A)this).foo; }
}

