/**
 * Test if the value of 'x' is the same that the value 
 * of 'index' (look at the aspect).
 */

import java.util.Random;
import org.aspectj.testing.Tester;

jpi void JP(int amount);

public class SimpleMatch{

    public static int index;
    public static Random rnd = new Random();

    exhibits void JP(int i):
        execution(* foo(..)) && args(i);

    void foo(int x){
        Tester.checkEqual(x,index,"x value is not equal to SimpleMatch.index, instead:"+x);
    }

    public static void main(String[] args){
        new SimpleMatch().foo(SimpleMatch.rnd.nextInt());
    }
}

aspect A{

    void around JP(int a){ 
        SimpleMatch.index = SimpleMatch.rnd.nextInt();
        proceed(SimpleMatch.index);
    }
}
