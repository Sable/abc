import org.aspectj.testing.Tester;

/*
 * Created on Jun 1, 2005
 *
 */
/**
 * @author Neil Ongkingco
 *
 */
public class EmptySigTestMain {

    public static void main(String[] args) {
        A a = new A();
        a.f();
        
        //matches on the print
        Tester.checkEqual(AspectA.callCtr, 1, "AspectA.callCtr"); 
    }
}
