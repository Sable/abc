package Access.test17;

/*
 *  Test 17 and 18: 
 *  Access class A respectively class A.B from inside method m (test should succeed)
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    void m() { 
        Test.A.B b;
    }
    class A {
        class B { }
    }
}
