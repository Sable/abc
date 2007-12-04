package Access.test38;

/*
 *  Test 38: 
 *  from a nested class, access a field in the superclass of a surrounding class which is hidden
 *  by a local field
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

class A {
    char bar;
}

public class Test extends A {
    char bar;
    class Inner1 {
        class Inner2 {
            char foo = ((A)Test.this).bar;
        }
    }
}
