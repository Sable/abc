/**
 * Test if the jpi definition is supported.
 */
import org.aspectj.testing.Tester;

jpi void foo();

public class JPI{

    void foo(int x){}

    public static void main(String[] args){
        new JPI().foo(5);
    }
}
