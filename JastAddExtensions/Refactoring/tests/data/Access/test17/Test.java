package Access.test17;

/*
 *  Test 17 and 18: 
 *  Access class A and class A.B from inside method m (test should succeed)
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    Class m() { 
        return A.class;
    }
    class A {
        class B { }
    }
}
