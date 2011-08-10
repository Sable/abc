/**
 * Test if the extends keyword is supported in the jpi definition.
 */

import org.aspectj.testing.Tester;

jpi void JP(int amount);
jpi void nai(char m, int i) throws IOException extends bar(3+4); //error, an expression can't be part of super name bindings.

public class InheritanceSyntax2{

    exhibits void JP(int i):
        execution(* foo(..)) && args(i);

    void foo(int x){}

    public static void main(String[] args){
        new InheritanceSyntax2().foo(5);
    }
}
