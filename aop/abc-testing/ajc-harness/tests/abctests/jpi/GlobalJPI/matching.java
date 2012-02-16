import java.lang.*;
import org.aspectj.testing.Tester;

<R> global jpi R JP() : execution(* *(..));

class A{ 
	public static Integer foo(){return null;}
}

class B{ 
	<L> exhibits L JP() : sealed();	
	public static Float foo(){return null;} //this join point won't be selected.
}

class AA{ 
	public static String foo(){return null;}
}

public class C{
	
	public static int counter=0;

	public static void main(String[] args){
		A.foo();
		B.foo();
		AA.foo();
		Tester.checkEqual(3, counter, "expected 3 matches but saw "+counter);		
	}
}

aspect AS{
	
	<L> L around JP(){
		System.out.println(thisJoinPoint.toString());
		C.counter++;
		return proceed();
	}	
}

//output of println -->

/*execution(void C.main(ClassNotFoundException))
 *execution(Integer A.foo())
 *execution(String AA.foo())
 */