//package alan;
//
//import org.aspectj.testing.Tester;
//
//jpi void JPInner();
//
//
//public class InnerClass{
//	
//	
//	class Inner{
//
//		exhibits void JPInner() : call(void *(..));
//				
//		public void foo(){
//			Tester.check(false,"foo error");
//		}
//		
//		public void bar(){
//			foo();
//		}
//		
//	}
//	
//	public static void main(String[] args){
//		InnerClass ic = new InnerClass();
//		Inner i = ic.new Inner();
//		i.bar();
//	}
//	
//}
//
//aspect InnerAspect{
//	
//	before JPInner(){
//		Tester.check(false,"");
//	}
//	
//	before() : call(void *(..)) && within(InnerClass.Inner){
//		
//	}
//}

import java.lang.*;
import org.aspectj.testing.Tester;

jpi void JP(Number l);

public class InnerClass{
	
	exhibits void JP(Number h) : call(* *(..)) && args(h);
	
	public static void foo(Integer i){}
	public static void bar(Integer i){}
	
	public static void main(String[] args){
		foo(5);
		bar(6);
	}
}

aspect A{
	
	before JP(Number a){
		Tester.check(false,"fail");
	}
	
}

