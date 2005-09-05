import org.aspectj.testing.Tester;

/*
 * Created on Jun 21, 2005
 *
 */

/**
 * @author Neil Ongkingco
 *
 */
public class ArgsTestMain {
    public static void main(String args[]) {
        A a = new A();
        a.f(1);
        a.f(2.0f);
        a.f(a);
        
        Tester.checkEqual(ArgsAspect.callCtr, 2, "ArgsAspect.callCtr");
    }
}
