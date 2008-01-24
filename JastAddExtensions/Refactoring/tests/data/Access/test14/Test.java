package Access.test14;

/*
 *  Test 14: 
 *  Access packages tests, Access.test14, and Access.test14.pkg1 from inside the anonymous
 *  class within method m (this test should fail).
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    void m() {
        Object o = new Object() {
            final int k;
            { k = 23; }
        };
    }
}

class Access { }