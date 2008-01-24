package Access.test20;

/*
 *  Test 20: 
 *  Access class Foo from inside method m, evading shadowing by local variable Foo 
 *  (test should succeed)
 *  
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    static class Foo { static double bar; }
    double m() { 
        int Foo = 23;
        return Foo;
    }
}
