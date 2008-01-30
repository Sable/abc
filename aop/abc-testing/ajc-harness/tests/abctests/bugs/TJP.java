import org.aspectj.lang.*;
import org.aspectj.testing.Tester;

aspect Aspect {
  JoinPoint jp;
  before() : call(* foo(..)) {
     jp = thisJoinPoint;
  }
  after() : call(* foo(..)) {
      if (jp != thisJoinPoint)
	  Tester.event("argh");
  }
}

public class TJP {
   static void foo() {}
   public static void main(String[] args) {
        foo();
	Tester.checkAllEvents();
   }
}
