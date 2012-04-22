import java.lang.*;
import org.aspectj.testing.Tester;

<R extends Integer, L extends Number>global jpi R JP(L a) : execution(R get*(..)) && Args(a); //warning

class A{
   <X extends Integer, U extends Number> exhibits X JP(U a) : global();

   public static Integer NotAGetName(Float b){  return null;}
   
   public static Integer call(){return NotAGetName(new Float(3));}
}

class B{

	<X extends Integer, U extends Number> exhibits X JP(U a) : call(* *(..)) && global(); //error
	
   public static Integer getQuantity(Integer a){  return null;}
}

public class C{
	
	public static Integer getSomething(Float b){return 2;}
	  
	public static void main(String[] args){
		A.call();
		B.getQuantity(5);
		getSomething(5.4f);
	}  
}