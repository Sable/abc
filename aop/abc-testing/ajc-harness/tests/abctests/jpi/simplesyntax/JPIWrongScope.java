/**
 * Test that the definition of exhibits only ocurrs inside of the class definition.
 */
import org.aspectj.testing.Tester;

jpi void JP(int amount);

public class JPIWrongScope{

	jpi void JP(int amount); //error

    exhibits void JP(int i) : 
    	execution (* SimpleSyntax2.foo(int)) && args(i);

    void foo(int x){}

    public static void main(String[] args){
        new JPIWrongScope().foo(5);
    }
}

aspect A{
	jpi void JP(int amount); //error	
}