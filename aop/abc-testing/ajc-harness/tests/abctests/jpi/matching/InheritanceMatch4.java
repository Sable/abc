/**
 * Test the order in which the piece of advices get executed.
 * Our semantics takes in account the advice definitior order and It doesn't
 * matter the jpi's definition order to this feature.
 */

import org.aspectj.testing.Tester;

jpi void JP(int amount);
jpi void JP2(int items, InheritanceMatch4 s) extends JP(items);
jpi void JP3(int x) extends JP(x);

public class InheritanceMatch4{

    public static int count = 0;

    exhibits void JP(int i):
        execution(* foo(..)) && args(i);

    exhibits void JP2(int z, InheritanceMatch4 i ):
        execution(void foo(int)) && args(z) && this(i);

    exhibits void JP3(int i):
        execution(* foo(..)) && args(i);


    void foo(int x){}

    public static void main(String[] args){
        new InheritanceMatch4().foo(6);
    }
}

aspect A{

    before JP(int a){
    	InheritanceMatch4.count++;
    }
    
    before JP2(int a, InheritanceMatch4 im){
    	InheritanceMatch4.count++;
        Tester.checkEqual(1,1,"expected that this advice gets executed first but saw"+InheritanceMatch4.count);    	    	
    }    

    before JP3(int a){
    	InheritanceMatch4.count++;
        Tester.checkEqual(2,2,"expected that this advice gets executed second but saw"+InheritanceMatch4.count);    	
    }

}
