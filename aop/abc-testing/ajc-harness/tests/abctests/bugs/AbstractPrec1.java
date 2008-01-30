import org.aspectj.testing.Tester;

abstract aspect Aspect {

    // abstract pointcut execfoo();
    pointcut execfoo() : execution(* foo(..));
    before() : execfoo() { Tester.event("before"); }
    after() : execfoo() && !cflowbelow(execfoo()) 
              {Tester.event("after");} 

}


aspect Concrete extends Aspect {

    

}


public class AbstractPrec1 {

    public static void foo() {}

    public static void main(String[] args) {
	AbstractPrec1.foo();
	Tester.expectEvent("before");
	Tester.expectEvent("after");
	Tester.checkAllEvents();
    }
}
