import org.aspectj.testing.Tester;

class A {
    public A(String x) {
    }
    public A(String x, int y) {
        this(new String(x));
    }
}

public class Preinit {
    public static void main(String[] args) {
        A a = new A("a",3);
    }
}

aspect JoinPointTraceAspect {

	pointcut tracePoints() : call(java.lang.String.new(String)) ||
                                  preinitialization(A.new(String, int));
	   
	after() throwing : tracePoints() {
	    System.out.println(thisJoinPoint);
	   
	}

      
} 
