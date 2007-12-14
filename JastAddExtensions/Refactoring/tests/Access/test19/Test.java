package Access.test19;

/*
 *  Test 19: 
 *  Access class Test from inside method m, evading shadowing by member field Test 
 *  (test should succeed)
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    static int Test;
    int m() { 
        return Test;
    }
}
