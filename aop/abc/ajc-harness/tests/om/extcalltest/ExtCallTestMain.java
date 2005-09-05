import org.aspectj.testing.Tester;

public class ExtCallTestMain {
    public static void main(String args[]) {
        A a = new A();
        int x = a.fib(5);
        System.out.println(x);
        int y = a.prevX; // for get test
        a.prevX = y; // for set test
        
        System.out.println(FibAspect.callCtr);
        System.out.println(FibExtAspect.callCtr);
        System.out.println(FibAspect.getCtr);
        System.out.println(FibExtAspect.getCtr);
        System.out.println(FibAspect.setCtr);
        System.out.println(FibExtAspect.setCtr);
        
        Tester.checkEqual(FibAspect.callCtr, 9, "FibAspect.callCtr");
        Tester.checkEqual(FibExtAspect.callCtr, 1, "FibExtAspect.callCtr");
        Tester.checkEqual(FibAspect.getCtr, 10, "FibAspect.getCtr");
        Tester.checkEqual(FibExtAspect.getCtr, 10, "FibExtAspect.getCtr");
        Tester.checkEqual(FibAspect.setCtr, 10, "FibExtAspect.setCtr");
        Tester.checkEqual(FibExtAspect.setCtr, 10, "FibExtAspect.setCtr");
    }
}
