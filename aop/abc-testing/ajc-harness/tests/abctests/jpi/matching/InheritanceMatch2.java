/**
 * Test the value of 'count' is 1 in presence of inheritance.
 * In this case IIIA fails.
 */

import java.util.Random;
import org.aspectj.testing.Tester;

jpi void JP(int amount);
jpi void JP2(int items, InheritanceMatch2 s) extends JP(items);
jpi void JP3(int x) extends JP(x);

public class InheritanceMatch2{

    public static int count = 0;

    exhibits void JP(int i):
        execution(* foo(..)) && args(i);

    exhibits void JP2(int z, InheritanceMatch2 i):
        execution(void foo(int)) && args(z) && this(i);

    exhibits void JP3(int i):
        execution(* foo(..)) && args(i);

    void foo(int x){}

    public static void main(String[] args){
        new InheritanceMatch2().foo(6);
        Tester.checkEqual(count,1,"expected 1 matches but saw"+count);
    }
}

aspect A{

    before JP(int a){
        InheritanceMatch2.count++;
    }
}
