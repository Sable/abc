package Access.test33;

/*
 *  Test 33: 
 *  access a field which is shadowed by a local variable
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    public char bar;
    char m() {
        int bar;
        return this.bar;
    }
}
