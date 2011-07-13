/**
 * Test that the definition of exhibits only ocurrs inside of the class definition.
 */
import org.aspectj.testing.Tester;

jpi void JP(int amount);

exhibits void JP(int i) : //error
	execution (* SimpleSyntax2.foo(int)) && args(i);

public class ExhibitWrongScope{

    exhibits void JP(int i) : 
    	execution (* SimpleSyntax2.foo(int)) && args(i);

    void foo(int x){}

    public static void main(String[] args){
        new ExhibitWrongScope().foo(5);
    }
}

aspect A{
    exhibits void JP(int i) : 
    	execution (* A.foo(int)) && args(i);

    void foo(int x){}    
}