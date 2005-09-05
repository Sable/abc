import java.util.*;

aspect ACache {
    public static int callCtr = 0;
    private Map cache = new HashMap();
    
    int around(int x) : Fib.fib(x){
        System.out.println("around " + thisJoinPoint.getSignature() + "in ACache");
        callCtr++;
        
        Integer i = (Integer)this.cache.get(new Integer(x));
        if (i != null) {
            return i.intValue();
        }
        Integer result = new Integer(proceed(x));
        cache.put(new Integer(x), result);
        return result.intValue();
    }
}