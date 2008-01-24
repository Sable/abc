package Access.test11;

/*
 *  Test 11: 
 *  Access packages tests, Access.test11, and Access.test11.pkg1 from inside the anonymous
 *  class within method m (this test should succeed).
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    void m() {
        Object o = new Object() {
            char foo;
        };
    }
}

