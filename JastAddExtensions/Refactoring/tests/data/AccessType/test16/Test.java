package Access.test16;

/*
 *  Test 16:
 *  Access interface A.B from inside method m (test should succeed)
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    int m() { 
        return A.foo;
    }
}

class A {
    static int foo = 42;
    interface B { }
}
