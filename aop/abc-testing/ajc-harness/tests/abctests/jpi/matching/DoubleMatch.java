/**
 * Test the value of 'count' should be 2 
 */

import org.aspectj.testing.Tester;

jpi void JP(int amount);
jpi void JP2(int items);

public class DoubleMatch{

    public static int countJP = 0; //advice JP counter
    public static int countJP2 = 0; //advice JP2 counter

    exhibits void JP(int i):
        execution(* foo(..)) && args(i);

    exhibits void JP2(int z):
        execution(void foo(int)) && args(z);

    void foo(int x){}

    public static void main(String[] args){
        new DoubleMatch().foo(6);
        Tester.checkEqual(DoubleMatch.countJP+DoubleMatch.countJP2,2,"expected 2 matches but saw "+DoubleMatch.countJP+DoubleMatch.countJP2);
    }
}

aspect A{

    void around JP(int a){//match first
        DoubleMatch.countJP++;
        Tester.checkEqual(DoubleMatch.countJP,1,"expected 1 match but saw "+DoubleMatch.countJP);
        proceed(a);
    }

    void around JP2(int b){
        DoubleMatch.countJP2++;
        Tester.checkEqual(DoubleMatch.countJP2,1,"expected 1 match but saw "+DoubleMatch.countJP2);        
    }
}
