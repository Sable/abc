public class Test {
   void foo() throws TestException {
      throw new TestException();
   }

   public static void main(String[] args)  {
      new Test().foo();
   }

}

class TestException extends Exception {

}

/*
aspect Aspect {

   declare soft: TestException: execution(void foo());

      
}
*/
