import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;


public class AllRuntime {
}

aspect A {
    before() : if(true) {
	System.out.println(thisJoinPoint);
    }
}

