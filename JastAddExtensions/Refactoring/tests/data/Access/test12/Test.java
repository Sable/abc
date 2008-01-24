package Access.test12;

/*
 *  Test 12: 
 *  Access packages tests, Access.test12, and Access.test12.pkg1 from inside the anonymous
 *  class within method m (this test should fail).
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    void m() {
        float Access;
        Object o = new Object() {
            char foo() { return 'a'; }
        };
    }
}

