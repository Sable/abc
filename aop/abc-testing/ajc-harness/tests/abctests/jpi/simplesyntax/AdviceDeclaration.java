/**
 * Test if the advice definition is supported.
 */
import org.aspectj.testing.Tester;

jpi void JP(int amount);

public class AdviceDeclaration{

    exhibit void JP(int i):
        execution(* foo(..)) && args(i);

    void foo(int x){}

    public static void main(String[] args){
        new AdviceDeclaration().foo(5);
    }
}

aspect A{
    before JP(int a){}

    void around JP(int a){ 
        proceed(a++);
    }
}
