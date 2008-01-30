/*
 * Created on Jul 27, 2005
 *
 */
import java.util.*;
import org.aspectj.testing.Tester;
/**
 * @author Neil Ongkingco
 *
 */
public class InheritPrecTestMain {

    static LinkedList callOrder = new LinkedList();
    public static void addToCallOrder(int x) {
        callOrder.add(new Integer(x));
    }
    
    public static void main(String[] args) {
        A a = new A();
        a.f1();
        
        Iterator iter = callOrder.iterator(); 
        for (int i = 1; i <= 3; i++) {
            Integer curr = (Integer) iter.next();
            Tester.checkEqual(i, curr.intValue());
        }
    }
}
