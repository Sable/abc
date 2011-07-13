/**
 * Test if the pointcut definition through the keyword exhibit is supported.
 */
import org.aspectj.testing.Tester;

jpi void JP(int amount);

public class Exhibit{

    exhibits void JP(int i) : 
    	execution (* SimpleSyntax2.foo(int)) && args(i);

    void foo(int x){}

    public static void main(String[] args){
        new Exhibit().foo(5);
    }
}

aspect A{
    exhibits void JP(int i) : 
    	execution (* A.foo(int)) && args(i);

    void foo(int x){}    
}
