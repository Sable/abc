
public class AroundPreinit {
   public static void main(String[] args) {
      new C();
   } 
}

class C { C() { } }

aspect A {
    void around(): staticinitialization(C) {
       proceed();
    }

    void around(): preinitialization(C.new()) {
       proceed();
    }
}
