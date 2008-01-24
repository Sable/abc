package Access.test37;

/*
 *  Test 37: 
 *  from a nested class, access a field in the superclass of a surrounding class
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

class A {
    char bar;
}

public class Test extends A {
    class Inner1 {
        class Inner2 {
            char foo = bar;
        }
    }
}
