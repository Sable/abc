/**
 * Test if the throws clause is supported in:
 * 1.- jpi definition
 * 2.- advice definition.
 */
import org.aspectj.testing.Tester;

jpi void JP1(int amount) throws Exception;
jpi void JP2(int items) throws Exception extends JP(items);

public class ThrowsSyntax{

    exhibits void JP(int i):
        execution(* foo(..)) && args(i);

    void foo(int x) throws Exception{}

    public static void main(String[] args){
        try{
            new ThrowsSyntax().foo(5);
        } catch (Exception e){
            e.printStackTrace();            
        }
    }
}

aspect A{

    void around JP(int a) throws Exception{ 
        proceed(a++);
    }
}
