/* from the Eclipse test suite */

import java.util.*;

class A {
    private List<? super Integer> al = new ArrayList<Integer>();

    void test() {
	/*[*/al.get(0)/*]*/;
    }
}