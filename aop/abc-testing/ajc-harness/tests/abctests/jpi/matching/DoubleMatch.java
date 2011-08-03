/**
 * Test the value of 'count' should be 2 
 */

import java.util.Random;
import org.aspectj.testing.Tester;

jpi void JP(int amount);
jpi void JP2(int items);

public class DoubleMatch{

    public static int count = 0;

    exhibits void JP(int i):
        execution(* foo(..)) && args(i);

    exhibits void JP2(int z):
        execution(void foo(int)) && args(z);

    void foo(int x){}

    public static void main(String[] args){
        new DoubleMatch().foo(6);
        Tester.checkEqual(count,2,"expected 2 matches but saw"+count);
    }
}

aspect A{

    void around JP(int a){
        DoubleMatch.count++;
        proceed(a);
    }

    void around JP2(int b){
        DoubleMatch.count++;
    }
}
