import org.aspectj.testing.Tester;

public class CflowInliningDepth {
    public static final void main(String[] args) {
        foo();
	Tester.expectEvent("Pointcut a applies");
	Tester.expectEvent("Pointcut b applies");
	Tester.checkAllEvents();
    }

    public static void foo() {
    }
}

aspect CIDAspect {
    pointcut callOfFoo() : call(void foo());
    pointcut b() : callOfFoo() && cflow( callOfFoo() && a() );
    pointcut a() : callOfFoo() && cflow( callOfFoo() );

    before(): a() {
        Tester.event("Pointcut a applies");
    }
    before(): b() {
        Tester.event("Pointcut b applies");
    }
}
