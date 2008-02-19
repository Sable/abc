package test;

public class Test02 {
  void test() {
  }
  int test;
  
  public static void main(String[] args) {
    // Trying to dereference a non null type results in a compile-time error
    Test02 t = new Test02();
    t.test(); // error
    int value = t.test; // error
    @NonNull Test02 u = new Test02();
    u.test(); // ok
    value = u.test; // ok
  }
}
