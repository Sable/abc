public class Test {

   void foo() {
   }

   void bar() {
      this.foo();
   }

   public static void main(String[] args) {
      new Test().bar();
      new Test2().bar();
   }

}

class Test2 extends Test {
//   void foo() {
//   }


   void bar() {
      this.foo();
   }
}

aspect Aspect {
   before() : call(void foo()) {
      System.out.println("Calling foo");
   }
}
