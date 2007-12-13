package Access.test40;

/*
 *  Test 40: 
 *  from a nested class, access a surrounding class
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    public char bar;
    class Inner1 {
        class Inner2 {
            int bar;
            { int aluis = Test.this.bar; }
        }
    }
}
