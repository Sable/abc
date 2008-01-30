
import org.aspectj.testing.Tester;

/** @testcase PR#889 after returning advice on interface constructor */
public class AfterReturningInterfaceConstructor {
    public static void main (String[] args) {
        Tester.expectEvent("constructor");
        Tester.expectEvent("advice");
        I i = new C();
        
        Tester.checkEqual(i.i, 2, "i.i");
        
        Tester.checkAllEvents();
    }     
}

interface I {}

class C implements I {
    C() {
        Tester.event("constructor");
    }
}

aspect A {
    int I.i;
	after(I i) returning: this(i) && initialization(I.new(..)) {
        i.i = 2;
    }
    after() returning: initialization(I.new(..)) {
        Tester.event("advice");
    }
}
