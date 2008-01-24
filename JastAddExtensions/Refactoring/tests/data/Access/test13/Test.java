package Access.test13;

/*
 *  Test 13: 
 *  Access packages tests, Access.test13, and Access.test13.pkg1 from inside the anonymous
 *  class within method m (this test should fail).
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    float Access;
    void m() {
        Object o = new Object() {
            int k;
            { k = 23; }
        };
    }
}

