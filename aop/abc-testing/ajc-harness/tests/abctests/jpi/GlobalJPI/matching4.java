import java.lang.*;
import org.aspectj.testing.Tester;

<R extends Integer>global jpi R JP() : execution(R get*(..));

class A{
   <X extends Integer> exhibits X JP() : global();

   public static Integer NotAGetName(){  return null;}
   
   public static Integer call(){return NotAGetName();}
}

class B{
   <M extends Integer> exhibits M JP() : global();

   public static Integer getQuantity(){  return null;}
}

public class C{
	
	public static int counter=0;
	
	public static Integer getSomething(){return 2;}
	public static Integer getSomethingElse(){return 2;}
	  
	public static void main(String[] args){
		A.call();
		B.getQuantity();
		getSomething();
		getSomethingElse();
		Tester.checkEqual(4, counter, "expected 4 matches but saw "+counter);
	}  
}

aspect AS{
	
	<L extends Integer> L around JP(){
		System.out.println(thisJoinPoint.toString());
		C.counter++;
		return proceed();
	}
}

//println output should be

/*call(int A.NotAGetName())
 * execution(int C.getSomething())
 * execution(int C.getSomethingElse())
 */
