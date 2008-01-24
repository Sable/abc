package Access.test32;

/*
 *  Test 32: 
 *  access a field which is shadowed by a parameter
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

public class Test {
    public char bar;
    char m(int bar) {
        return this.bar;
    }
}
