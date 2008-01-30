
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

/**
 * Run via main or driveTest.
 * If you want the verbose output,
 * use "-p{rint}" to print when invoking via main,
 * or set resultCache with your result sink before running:
 * <pre>StringBuffer sb = new StringBuffer();
 * AllRuntime.resultCache(sb);
 * int errors = AllRuntime.driveTest();
 * System.err.println(sb.toString());
 * System.err.println("Errors: " + errors);</pre>
 * <p>
 * This was written to run in a 1.1 VM,
 * outside the Tester or Collections or...
 * 
 * @testcase PR#474 rt.java uses 1.2-only variant of Class.forName 
 */ 
public class OddException {
    public static void resultCache(StringBuffer cache) {
        A.resultCache(cache);
    }

    public static void main(String[] args) {
        StringBuffer result = null;
        if ((null != args) && (0 < args.length) 
            && (args[0].startsWith("-p"))) {
           result = new StringBuffer();
           resultCache(result);
        }
        int errors = driveTest();
        A.log("Errors: " + errors);
        if (null != result) {
            System.err.println(result.toString());
        }
    }

    /** @return number of errors detected */
    public static int driveTest() {
        int result = 0;
        boolean ok = testNoAspectBoundException();
        if (!ok) result++;
        A.log("testNoAspectBoundException: " + ok);
        ok = testMultipleAspectsBoundException();
        if (!ok) result++;
        A.log("testMultipleAspectsBoundException: " + ok);

        TargetClass me = new TargetClass();

        ok = me.catchThrows();
        if (!ok) result++;


        int temp = me.publicIntMethod(2);
        if (temp != 12) result++;

        StringBuffer sb = new StringBuffer();
        sb.append("" + me);  // callee-side join point
        if (sb.length() < 1) result++;
        A.log("Callee-side join point " + sb.toString());

        try {
            ok = false;
            me.throwException = true;
            me.run();
        } catch (SoftException e) {
            ok = true;
        }
        if (!ok) result++;
        A.log("SoftException: " + ok);
        A a = A.aspectOf();
        if (null != a) {
            ok = a.report();
            if (!ok) result++;
            A.log("  => all advice was run: " + ok);
        }
        return result;
    }

    /** todo: need test case for multiple aspects */
    public static boolean testMultipleAspectsBoundException() {
        return true;
    }

    public static boolean testNoAspectBoundException() {
        boolean result = false;
        try {
            B a = B.aspectOf(new Object());
        } catch (NoAspectBoundException e) {
            result = true;
        }
        return result;
    }
}

/*
/** This has all relevant join points */
class TargetClass {
    private static int INDEX;
    static {
        INDEX = 10;
    }
    private int index = INDEX;
    private int shadow = index;

    public int publicIntMethod(int input) { 
        return privateIntMethod(input); 
    }

    public boolean catchThrows() {
        try {
            throw new Exception("hello");
        } catch (Exception e) {
            if (null != e) return true;
        }
        return false;
    }

    /** print in VM-independent fashion */
    public String toString() { 
        return "TargetClass " + shadow; 
    }

    private int privateIntMethod(int input) { 
        return shadow = index += input; 
    }
}
*/
/*
/** used only for NoAspectBoundException test */
aspect B perthis(target(TargetClass)) { }
*/
aspect A {
    /** log goes here if defined */
    private static StringBuffer CACHE;
    /** count number of join points hit */
    private static int jpIndex = 0;
    /** count number of A created */
    private static int INDEX = 0;
    /** index of this A */
    private int index;
    /** count for each advice of how many times invoked */
    private final int[] adviceHits;
    A() { 
        index = INDEX++; 
        adviceHits = new int[21];
    }

    public static void resultCache(StringBuffer cache) {
        if (CACHE != cache) CACHE = cache;
    }

    public static void log(String s) { 
        StringBuffer cache = CACHE;
        if (null != cache) {
            cache.append(s);
            cache.append("\n");
        }
    }

    private void log(int i) { adviceHits[i]++; }

    /** report how many times each advice was run
     * logging report.
     * @return false if any advice was not hit 
     */
    public boolean report() { 
        StringBuffer sb = new StringBuffer();
        boolean result = report(this, sb);
        log(sb.toString()); 
        return result;
    }

    /** report how many times each advice was run
     * @return false if any advice was not hit 
     */
    public static boolean report(A a, StringBuffer sb) { 
        boolean result = true;
        if (null == a.adviceHits) {
            sb.append("[]");
        } else {
            sb.append("[");
            int[] adviceHits = a.adviceHits;
            for (int i = 0; i < adviceHits.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(i+"="+adviceHits[i]);
                if (result && (0 == adviceHits[i])) {
                    result = false;
                }
            }
            sb.append("]");
        }
        return result;
    }

    public static void throwsException() throws Exception { 
        throw new Exception("exception"); 
    }
    public String toString() { return  "A " + index; }

    //-------------------------------------- declare, introductions
    declare parents : TargetClass implements Runnable;
    
    /** unused - enable to throw exception from run() */
    public boolean TargetClass.throwException;
    public void TargetClass.run() {
        if (throwException) throwsException();
    }

    /** if pcd join point */
    before(int i) : args(i) && if(i > 0) {
	System.out.println(thisJoinPoint);
    }
}

