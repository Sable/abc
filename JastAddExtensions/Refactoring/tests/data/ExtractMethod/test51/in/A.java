/* from the Eclipse test suite */

import java.util.List;

class A<T> {
    private int test(List<T> list) {
	return /*[*/list.size()/*]*/;
    }
}