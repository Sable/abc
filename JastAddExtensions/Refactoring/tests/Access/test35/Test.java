package Access.test35;

/*
 *  Test 35: 
 *  from a nested class, access a field in a surrounding class
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    public char bar;
    class Inner1 {
        class Inner2 {
            char foo = bar;
        }
    }
}
