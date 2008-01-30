import org.aspectj.testing.Tester;

public class InlinedCons {
  InlinedCons() {
     this(null);
  }

  InlinedCons(InlinedCons i) {
     foo();
  }
  public static void foo() { }
  public static void main(String[] args) {
     new InlinedCons();
     Tester.expectEvent("foo called");
     Tester.checkAllEvents();
  }
}

aspect ICAspect {
  before() : initialization(InlinedCons.new(..)) { }

  before() : call(void InlinedCons.foo()) {
     Tester.event("foo called");
  }
}
