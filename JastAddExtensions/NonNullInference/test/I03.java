package test;

public class I03 {
  Object f = new Object();

  void test(Object p) {
    Object v;
    f = p = v = new Object();
    f = p = v = null;
  }
  
  public static void main(String[] args) {
    System.out.println("possibly-null is a supertype of non-null");
  }
}
