public class AroundAR {
    public static void main(String[] args) {
	int x=foo();
//	System.out.println(x);
    }
    static int foo() { return bar(); }
    static int bar() { return 2; }
    
}

aspect AspectAAR {
    pointcut foo() : execution(int foo());
	int around() : execution(int foo()) {
	return proceed();
    }

    after () returning (int y) : execution(int foo()) {
    }
}
