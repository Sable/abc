/**
 * Test the value of 'count' is 1 in presence of inheritance.
 */
import org.aspectj.testing.Tester;

jpi void JP(int amount);
jpi void JP2(int items, InheritanceMatch s) extends JP(items);

public class InheritanceMatch{

    public static int count = 0;

    exhibits void JP(int i):
        execution(* foo(..)) && args(i);

    exhibits void JP2(int z, InheritanceMatch i ):
        execution(void foo(int)) && args(z) && this(i);

    void foo(int x){}

    public static void main(String[] args){
        new InheritanceMatch().foo(6);
        Tester.checkEqual(count,1,"expected 1 matches but saw"+count);
    }
}

aspect A{

    before JP(int a){
        InheritanceMatch.count++;
        Tester.checkEqual(false,"should not execute");
    }

    before JP2(int b, InheritanceMatch im){
        InheritanceMatch.count++;
    }
}
