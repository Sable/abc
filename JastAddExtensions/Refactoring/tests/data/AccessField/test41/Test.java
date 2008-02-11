package Access.test41;

class A {
    int i;
    class B {
        int i;
    }
}

public class Test {
    A.B x;
    int m() {
    	// there seems to be no way of referring to the i in A through x
        return x.i;
    }
}