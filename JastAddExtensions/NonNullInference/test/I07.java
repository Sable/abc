package test;

public class I07 {
  Object f = new Object();
  public I07() {
    test();
  }
  
  void test() {
    Object v = f;
  }

  public static void main(String[] args) {
    System.out.println("implicit this parameter makes non-null field possibly null");
  }
}
