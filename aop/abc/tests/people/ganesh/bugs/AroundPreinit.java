
public class AroundPreinit {
   public static void main(String[] args) {
      new C();
   }
}

class C {
    C() {
        this(foo());
    }

    C(int x) {
    }

    static int foo() {
        return 5;
    }
}

aspect A {
    void around(): preinitialization(C.new()) {
       proceed();
    }
}
