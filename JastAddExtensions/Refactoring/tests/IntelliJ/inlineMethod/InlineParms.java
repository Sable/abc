public class Test {
    /*[*/public int foo(int p1, int p2) {
        p2++;
        return someMethod(p1, p2);
    }/*]*/

    public void use1() {
	// changed next line for compilability
        int r = foo(23, 42);
    }

    public void use2() {
        int r = foo(field1, field1);
    }

    public void use3() {
        int r = foo(field2, field2);
    }

    public void use4() {
        int r = foo(field3, field3);
    }

    private final int field1;
    private int field2;
    private int field3;

    // moved initializer after fields for compilability
    {
        field2++;
    }

    // added following line for compilability
    int someMethod(int p1, int p2) { return p1; }
}