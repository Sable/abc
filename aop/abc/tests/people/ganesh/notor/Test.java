public class Test  {
   public void foo() {}

   public static void main(String[] args) {
      new Test().foo();
   }      
}

class Baz extends Test {}
class Blat extends Test {}

aspect Aspect {
   before() : call(void foo()) && !target(Baz) {}
   before() : call(void foo()) && (target(Baz) || target(Blat)) {}
}
