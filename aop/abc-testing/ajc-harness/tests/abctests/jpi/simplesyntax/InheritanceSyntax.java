/**
 * Test if the extends keyword is supported in the jpi definition.
 */

import org.aspectj.testing.Tester;

jpi void JP(int amount);
jpi void JP2(int items) extends JP(items);
jpi void bar(int z, Object x) throws Exception extends foo(a,b);
jpi void nai(char m) throws IOException extends bar(3+4); //error, an expression can't be part of super name bindings.

public class SimpleSyntax3{

    exhibit void JP(int i):
        execution(* foo(..)) && args(i);

    void foo(int x){}

    public static void main(String[] args){
        new SimpleSyntax().foo(5);
    }
}
