package Access.test36;

/*
 *  Test 36: 
 *  from a nested class, access a field in a surrounding class which is hidden by a local field
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
