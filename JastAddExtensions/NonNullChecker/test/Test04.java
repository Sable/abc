package test;

public class Test04 {
  public static void main(String[] args) {
    // the type of a parameter, local variable, or instance variable
    // can be changed by guarding it with an explicit null check
  
    Test04 t = new Test04();
    t.test(); // error, the type of t is Test04 here
    t = null; // ok
    if(t != null) {
      t.test(); // ok, the type of t is [NotNull] Test04 here
      t = null; // error
    }
  }
  
  public void testParameterAndField(Test04 self) {
    self.test(); // error, the type of self is Test04 here
    self = null; // ok
    if(self != null) {
      self.test(); // ok the type of self is [NotNull] Test04 here
      self = null; // error
    }

    field.test(); // error, the type of field is Test04 here
    field = null; // ok
    if(field != null) {
      field.test(); // ok the type of field is [NotNull] Test04 here
      field = null; // error
    }
  }


  public void test() {
  }
  public Test04 field = new Test04();

  
}
