/**
 * Test if the throws clause is supported in:
 * 1.- jpi definition
 * 2.- advice definition.
 */
import org.aspectj.testing.Tester;

jpi void JP1(int amount) throws Exception;
jpi void JP2(int items) throws Exception extends JP1(items);

public class ThrowsSyntax{

    exhibits void JP1(int i):
        execution(* foo(..)) && args(i);

    void foo(int x) throws Exception{}
}

aspect A{

    void around JP1(int a) throws Exception{ 
        proceed(a++);
    }
}
