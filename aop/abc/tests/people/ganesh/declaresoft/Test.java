public class Test {
   void foo() {
      throw new TestException();
   }

   public static void main(String[] args) {
      new Test().foo();
   }

}

class TestException extends Exception {

}



abstract aspect Aspect {

   declare soft: TestException: execution(void foo());

      
}

aspect Aspect2 extends Aspect {
}

aspect Aspect3 extends Aspect {
}

