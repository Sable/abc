
/* boiled down version of Laddad 10.6-v2. */


abstract aspect Aspect {

    // abstract pointcut execfoo();
    pointcut execfoo() : execution(* foo(..));
    before() : execfoo() { System.out.println("before"); }
    after() : execfoo() && !cflowbelow(execfoo()) 
              {System.out.println("after");} 

}


aspect Concrete extends Aspect {

    

}


public class AbstractPrec1 {

    public static void foo() {}

    public static void main(String[] args) {
	AbstractPrec1.foo();
    }
}