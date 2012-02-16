import java.lang.*;
import org.aspectj.testing.Tester;

<R> global jpi R JP() : call(* *(..)); //(!)
//(!) we can get stuck in an infinite regression if
//the around advice defined below gets executed.

class A{ 
	public static Integer foo(){return null;}
}

class B{ 
	public static Float foo(){return null;}
}

class AA{ 
	public static String foo(){return null;}
}

public class C{
	
	public static int counter = 0;
	
	public static void main(String[] args){
		A.foo();
		B.foo();
		AA.foo();
		Tester.checkEqual(3, counter, "expected 3 matches but saw "+counter);
	}
}

aspect AS{
	
	<L> exhibits L JP() : sealed(); //(!!)
	//(!!) We sealed the aspect to avoid
	// infinite regression.  I think this is nicer
	// than put !cflow(adviceexecution()) in the
	// global jpi pointcut expression.
	
	<L> L around JP(){
		System.out.println(thisJoinPoint.toString());
		C.counter++;
		return proceed();
	}	
}

//println output should be

/* call(Integer A.foo())
 * call(Float B.foo())
 * call(String AA.foo())
 */
