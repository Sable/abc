/**
 * Test that the execution of proceed doesn't trigger
 * the execution of the advice associated with jpi named JP.
 */

import org.aspectj.testing.Tester;

jpi int JP(int amount);
jpi int JP2(int amount) extends JP(amount);

public class DontMatch{

    exhibits int JP(int i):
        call(* foo(..)) && args(i);

    exhibits int JP2(int i):
        call(* bar(..)) && args(i);

    int foo(int x){
        return x;
    }

    int bar(int x){
        return x;
    }

    public static void main(String[] args){
        new DontMatch().bar(6);
    }
}

aspect A{

    int around JP(int a){ 
        Tester.check(false,"should not execute");
        return a;
    }

    int around JP2(int a){ return proceed(a);}

}
