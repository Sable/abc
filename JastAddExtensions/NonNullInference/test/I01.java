package test;

public class I01 {
  Object f = new Object();

  void test(Object p) {
    Object v;
    f = p = v = new Object();
  }
  
  public static void main(String[] args) {
    System.out.println("simple inference of non-null property for variables");
  }
}
