package Access.test34;

/*
 *  Test 34: 
 *  access a field in the superclass which is shadowed by both a local variable and a field
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

class A {
    public char bar;
}

public class Test extends A {
    public char bar;
    char m() {
        int bar;
        return super.bar;
    }
}
