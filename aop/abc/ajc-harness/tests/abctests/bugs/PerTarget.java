public class PerTarget {
   public static void main(String[] args) {
      new PerTarget().foo();
   }

   void foo() {
      bar();
   }

   void bar() {
   }
}

aspect PTAspect pertarget(call(void foo())) {
   before () : call(void bar()) {
   }
}
  
