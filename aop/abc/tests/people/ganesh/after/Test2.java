public class Test2 {
   void foo() throws TestException {
      throw new TestException();
   }

   public static void main(String[] args) throws TestException {
      new Test2().foo();
   }

}

class TestException extends Exception {

}

aspect Aspect2 {

   after () throwing(TestException e) : execution(void foo()) {

   }

      
}
