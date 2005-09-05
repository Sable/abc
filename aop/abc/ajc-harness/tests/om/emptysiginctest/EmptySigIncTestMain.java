import org.aspectj.testing.Tester;

/*
 * Created on Jun 1, 2005
 *
 */
/**
 * @author Neil Ongkingco
 *
 */
public class EmptySigIncTestMain {

    public static void main(String[] args) {
        A.f1();
        A.f2();
        A.f3();
        B.f1();
        B.f2();
        B.f3();
        C.f1();
        C.f2();
        C.f3();
        
        //matches on the print
        Tester.checkEqual(AspectA.callCtr, 3, "AspectA.callCtr"); 
    }
}
