public class Test5 {
    public static void main(String[] args) {
	foo();
	foo();
	foo();
    }
    static void foo() {}
}

aspect Test5Aspect {
    before() : within(Test5) && cflow(call(void foo())) { }
}