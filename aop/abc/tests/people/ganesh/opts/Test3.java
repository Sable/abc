public class Test3 {
    public void foo() {
	foo();
    }
}

aspect Test3_Aspect {
    before() : cflow(call(void foo())) {
    }
}
