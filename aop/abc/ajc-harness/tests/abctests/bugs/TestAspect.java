public class TestAspect {
    public static final void main(String[] args) {
        foo();
    }
    public static void foo() {
    }
}

aspect TestAspectAspect {
    pointcut callOfFoo() : call(void TestAspect.foo());
    pointcut a() : cflow( callOfFoo() && b() );
    pointcut b() : cflow( callOfFoo() && a() );

    before(): a() {
        System.out.println( "Pointcut a applies." );
    }
    before(): b() {
        System.out.println( "Pointcut b applies." );
    }
}
