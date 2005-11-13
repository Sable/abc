import org.aspectj.bridge.AbortException;

class A {
    public A(String x) {
    }
    public A(String x, int y) {
        this(new String(x));
    }
}

public class Preinit2 {
    private static int maxEvents = 100;
    private static String[] events = new String[maxEvents];
    private static int nexteventreport = 0;
    private static int nexteventcheck = 0;

    public static void reportEvent(String s)
    {
        if (nexteventreport >= maxEvents)
          throw new AbortException("Too many events");
        events[nexteventreport++] = s;
    }

    public static void checkEvent(String s)
    {
        if (nexteventcheck >= nexteventreport)
          throw new AbortException("Missing event: "+s);
        if (!events[nexteventcheck].equals(s))
          throw new AbortException("Got event: "+events[nexteventcheck]+" expected: "+s);
        nexteventcheck++;
    }

    public static void allEventsChecked()
    {
       if (nexteventreport > nexteventcheck)
         throw new AbortException("Unexpected event: "+events[nexteventcheck]);
    }

    public static void main(String[] args) {
        A a = new A("a",3);
        checkEvent("before preinit");
        checkEvent("before new");
        checkEvent("after new");
        checkEvent("after preinit");
        allEventsChecked();
    }
}

aspect JoinPointTraceAspect {

    before() : call(java.lang.String.new(String)) {
       Preinit2.reportEvent("before new");
    }

    after() : call(java.lang.String.new(String)) {
       Preinit2.reportEvent("after new");
    }
    
    before() : preinitialization(A.new(String, int)) {
       Preinit2.reportEvent("before preinit");
    }


    after() : preinitialization(A.new(String, int)) {
       Preinit2.reportEvent("after preinit");
    }
} 
