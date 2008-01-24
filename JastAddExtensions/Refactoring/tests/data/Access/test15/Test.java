package Access.test15;

/*
 *  Test 15:
 *  Access class A from inside method m (test should succeed)
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    int foo = 42;
    int m() { 
        return foo;
    }
}

class A {
    interface B { }
}
