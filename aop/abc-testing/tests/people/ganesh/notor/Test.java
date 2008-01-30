public class Test  {
   public void foo() {}
   public void bar() {new Test().foo();}

   public static void main(String[] args) {
      new Test().bar();
   }      
}

class Baz extends Test {}
class Blat extends Test {}

aspect Aspect {
   before() : call(void foo()) && !target(Baz) {}
   before() : call(void foo()) && (target(Baz) || target(Blat)) {}
   before(Baz x) : call(void foo()) && (this(x) || target(x)) {}
}
