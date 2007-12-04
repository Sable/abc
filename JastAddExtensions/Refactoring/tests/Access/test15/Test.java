package Access.test15;

/*
 *  Test 15 and 16: 
 *  Access class A respectively interface A.B from inside method m (test should succeed)
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    void m() { 
        int foo = 0;
    }
}

class A {
    interface B { }
}
