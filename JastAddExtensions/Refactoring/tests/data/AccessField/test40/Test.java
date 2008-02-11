package Access.test40;

/*
 *  Test 40: 
 *  Access a field from a surrounding class which is shadowed by an inherited
 *  field.
 *
 *  If you change this file, also change tests; positions are hard-coded!
 */

class A {
    String foo;
}

public class Test {
    char[] foo;
    class B extends A {
        char[] m() {
            return Test.this.foo;
	}
    }
}

