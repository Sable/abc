import java.lang.*;
import org.aspectj.testing.Tester;

global jpi int JP() : execution(int get*(..));

Open class A{
   exhibits int JP() : call(int NotAGetName(..)) || global();

   public static int NotAGetName(){  return 1;}
   
   public static int call(){return NotAGetName();}
}

class B{
   exhibits int JP();

   public static int getQuantity(){  return 1;}
}

Open public class C{
	
	public static int counter=0;
	
	public static int getSomething(){return 2;}
	public static int getSomethingElse(){return 2;}
	  
	public static void main(String[] args){
		A.call();
		B.getQuantity();
		getSomething();
		getSomethingElse();
		Tester.checkEqual(3, counter, "expected 3 matches but saw "+counter);
	}  
}

aspect AS{
	
	int around JP(){
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
